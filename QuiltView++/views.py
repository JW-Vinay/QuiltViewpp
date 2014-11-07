from flask import (
    Flask,
    abort,
    flash,
    redirect,
    render_template,
    request,
    session,
    url_for,
)
from flaskext.mysql import MySQL
import os, subprocess

mysql = MySQL()
app = Flask(__name__)

#DATABASE = '/tmp/quiltview.db'
DEBUG = True
SECRET_KEY = 'A0Zr98j/3yX R~XHH!jmN]LWX/,?RT'
STREAM_SERVER_IP = '127.0.0.1'
STREAM_SERVER = 'vm005.elijah.cs.cmu.edu'
app.config['MYSQL_DATABASE_USER'] = 'root'
app.config['MYSQL_DATABASE_PASSWORD'] = 'root'
app.config['MYSQL_DATABASE_DB'] = 'quiltview'
app.config['MYSQL_DATABASE_HOST'] = 'localhost'
mysql.init_app(app)


# launch flask configurations
app.config.from_object(__name__)
app.config.from_envvar('FLASKR_SETTINGS', silent=True)


@app.route('/')
def showHome():
	error = None
	if 'logged_in' in session and session.get('logged_in'):
		return render_template('home.html')
	else:
		return render_template('login.html')

@app.route('/signup', methods=['GET','POST'])
def signup():
	error = None
	if request.method == 'POST':
		username = request.form['username']
    		password = request.form['password']
		firstname = request.form['firstname']
    		lastname = request.form['lastname']
		cursor = mysql.connect().cursor()
		cursor.execute("SELECT * from User where username='" + username + "' and password='" + password + "'")
    		data = cursor.fetchone()
		if data is None:
			query = "INSERT into User(username, password, firstname, lastname) VALUES('%s','%s','%s','%s');" % (username, password, firstname, 				lastname)
			cursor.execute(query)
    		 	cursor.connection.commit()
    			cursor.close()
			session['logged_in'] = 'True'
			flash('You were logged in')
			return redirect(url_for('showHome'))	
		else:
		     	flash('User already exists')
			return render_template('login.html', error=error)	
	return render_template('signup.html', error=error)

@app.route('/login', methods=['GET', 'POST'])
def login():
	error = None
	if request.method == 'POST':
		username = request.form['username']
    		password = request.form['password']
    		cursor = mysql.connect().cursor()
    		cursor.execute("SELECT * from User where username='" + username + "' and password='" + password + "'")
    		data = cursor.fetchone()
    		if data is None:
     			error = 'Invalid username'
    		else:
			flash('You were logged in')
			session['logged_in'] = 'True'     		
			return redirect(url_for('showHome'))
     	return render_template('login.html', error=error)

def call_command(command):
    f = open('/dev/null','rw')
    subprocess.Popen(command.split(' '))

@app.route('/stream')
def stream():
	ip = request.remote_addr
	ip = "128.237.221.39:1234"
	#call_command("avconv -i rtsp://%s:1234 -ar 44100 -f flv rtmp://127.0.0.1:1935/mytv/vinay-phone" % (ip))
	call_command("avconv -i rtsp://%s -ar 44100 -r 30 -vsync cfr -q 30 -fflags nobuffer -f flv rtmp://%s:1935/mytv/vinay-phone" % (ip, STREAM_SERVER_IP))
	return render_template('index.html', stream_name="vinay-phone", stream_url="rtmp://%s:1935/mytv/" % (STREAM_SERVER))

@app.route('/count-subscribers', methods = ['POST'])
def getNoSubscribers():
	if request.method == 'POST':
		print request.form

	return ""

@app.route('/logout')
def logout():
    session.pop('logged_in', None)
    flash('You were logged out')
    return redirect(url_for('showHome')) 
	
if __name__ == '__main__':
    app.run(host='0.0.0.0')

