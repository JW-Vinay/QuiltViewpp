from flask import (
    Flask,
    abort,
    flash,
    redirect,
    render_template,
    request,
    session,
    url_for,
    Response,
    json,
    jsonify
)


from flaskext.mysql import MySQL
from MySQLdb import IntegrityError
import os, subprocess
import collections
import uuid

mysql = MySQL()
app = Flask(__name__)

#DATABASE = '/tmp/quiltview.db'
DEBUG = True
SECRET_KEY = 'A0Zr98j/3yX R~XHH!jmN]LWX/,?RT'
STREAM_SERVER = 'vm005.elijah.cs.cmu.edu'
STREAM_SERVER_IP = '127.0.0.1'
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

@app.route('/submit-query', methods = ['POST'])
def submitQuery():
	if request.method == 'POST':
		query = request.form['query']
		isRefresh = request.form['refresh_status']
		print query, isRefresh
		if (query is not None) and (query != ''):
	    		cursor = mysql.connect().cursor()
			query = query.strip()
	    		queryString = "SELECT a.query_id, a.query_item, b.stream_id, b.is_stream_active, b.rtmp_url FROM queries a INNER JOIN live_streams b ON a.query_id = b.query_id WHERE is_stream_active=true and a.query_item='%s';" % (query)
	    		cursor.execute(queryString)
	    		if cursor.fetchone() is None:
	    			#queryString  = "SELECT * FROM queries WHERE query_item='%s';" %(query)
	    			#cursor.execute(queryString)
	    			#if cursor.fetchone() is None:
	    			if (isRefresh is not None) and isRefresh == "true":
					print isRefresh
					data = {"message":"Duplicate entry. Please try again.", "status":-1, "count" : 0}
				else:
					#insert to query table check value of no_subscribers update only play event is received from nginx
			 		queryString = "INSERT into queries(query_item, no_active_streamers, no_subscribers) VALUES('%s','%s', %s);" % (query, 0,0)
			 		try:
			    			cursor.execute(queryString)
		    			 	cursor.connection.commit()
		    			 	data = 	{"message":"Waiting for response.", "status":200, "count" : 0}
						resp = json.dumps(data)
																
					except IntegrityError as e:
						#data = 	{"message":"Duplicate entry. Please try again.", "status":-1, "count" : 0}
						#resp = 	json.dumps(data)
						queryString  = "UPDATE queries SET timestamp  = now()  WHERE query_item = '%s';" % (query)
						cursor.execute(queryString)
						cursor.connection.commit()
						data =  {"message":"Waiting for response.", "status":200, "count" : 0}
					
				resp = json.dumps(data)
				print resp
				return resp	
	    		else:
	    			# commenting subscriber count update if query is already present in the database
	    			#querySet = cursor.fetchone();
	    			streamList = generateJsonForQueries(cursor, "true")
	    			#queryString  = "update queries set no_subscribers = no_subscribers +1 where query_id=%s;" %(querySet[0])
	    			#cursor.execute(queryString)
	    			#cursor.connection.commit()
	    			
		    		if streamList:
			    		resp = json.dumps({"queries": streamList, "count": len(streamList), "status": 200})
					#print resp
					return resp
		#			return render_template('home.html', data=resp, empty=None)
				else:
					data = 	{"message":"list is empty", "status":200, "count" : 0}
					resp = json.dumps(data)
		#			resp = jsonify(data)
					#resp.status_code = 200;
					#resp.mimetype = "application/json"	     	
					return resp
		#		return render_template('home.html', data=None, empty=data)
		else:
			return redirect(url_for('showHome'))	
		
    		 
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
			query = "INSERT into User(username, password, firstname, lastname) VALUES('%s','%s','%s','%s');" % (username, password, firstname,lastname)
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

def errorResponse(message = None, status = None):
	data = {}
	if message is None:
		data["message"] = "missing headers" 
	else:
		data["message"] = message 	

	if status is None:
		data["status"] = "404"
		status = 404;
	else:
		data["status"] = status
			

	resp = jsonify(data)
	resp.status_code = status; 
	resp.mimetype = "application/json"
	return resp

def createPostReplyResponse(message = None, id = None):
	data = {}
	if message is None:
		data["message"] = "start streaming" 
	else:
		data["message"] = message 	
	
	if id is not None:
		data["id"] = id
	
	data["status"] = 200
	resp = jsonify(data)
	resp.status_code = 200;
	resp.mimetype = "application/json"
	return resp
	
def checkRequestHeaders(checkContentType = None):
	
	if "device_type" in request.headers: 
		devid = request.headers['device_type']
		contentType = None
		if "Content_Type" in request.headers:
			contentType = request.headers['Content_Type']
			
		if (devid is None or not devid or len(devid) < 14) \
			and ((checkContentType is not None and contentType != "application/x-www-form-urlencoded") \
			or checkContentType is None):
			return errorResponse()
		else:
			return None
	else:
		return errorResponse()
		
@app.route('/count-subscribers', methods = ['GET','POST'])
def getNoSubscribers():
	if request.method == 'POST':
		streamName = request.form['name']
		callType = request.form['call']
		print "name action", callType
		streamUrl = "rtmp://%s:1935/mytv/%s" % (STREAM_SERVER, streamName)
		streamId = "SELECT query_id FROM live_streams WHERE rtmp_url='%s'" %(streamUrl)
		#print "stream id ", streamId
		if callType == "play":
			queryString  = "UPDATE queries SET no_subscribers = no_subscribers +1, timestamp = timestamp  WHERE query_id= (%s);" %(streamId)
		else:
			queryString  = "UPDATE queries SET no_subscribers = no_subscribers -1, timestamp = timestamp WHERE query_id= (%s) and no_subscribers > 0;" %(streamId)
		#print "query  ", queryString
		cursor = mysql.connect().cursor()
		cursor.execute(queryString)	
		cursor.connection.commit()
		return ""
	elif request.method == 'GET':
		val = checkRequestHeaders("true")
		if val is not None:		
			return val
		else:
			query_id = request.args['qid']
			queryString = "SELECT * FROM queries WHERE query_id= %s;" %(query_id) 	
			cursor = mysql.connect().cursor()
			cursor.execute(queryString)
			data = generateJsonForQueries(cursor)
			resp = jsonify({"data": data, "status": 200})
			resp.status_code = 200
			resp.mimetype = "application/json"		
			return resp
				
			
@app.route('/quit-stream', methods = ['POST'])
def quitStream():
	val = checkRequestHeaders("true")
	if val is not None:		
		return val
	
	if request.method == 'POST':
		queryId = request.form['query_id']
		streamId = request.form['stream_id']
		if queryId is not None and streamId is not None:
			cursor = mysql.connect().cursor()
			query = "SELECT pid FROM live_streams WHERE stream_id=%s" % (streamId)
			cursor.execute(query)
			cursorTuples = cursor.fetchone()
			if cursorTuples is not None:
				pid = cursorTuples[0]
				#print "pid", pid
				killFFMPEG(pid)	
				query = "UPDATE queries SET no_active_streamers = no_active_streamers-1, timestamp = timestamp WHERE query_id = %s and no_active_streamers > 0;" % (queryId)
				cursor.execute(query)	
				cursor.connection.commit()
				query  = "SELECT no_active_streamers FROM queries WHERE query_id = %s;" % (queryId)
				cursor.execute(query)
				streamersCount = cursor.fetchone()
				resp = Response(response=None, status=204, mimetype=None)
				if streamersCount is not None:
					 count = streamersCount[0]
					 if count <= 0:
					 	# set stream active bit to 0 when no of streamers is 0 and set no_subscribers to 0 for this case as well
					 	query = "UPDATE live_streams SET is_stream_active = 0 WHERE stream_id = %s;" % (streamId)
					 	cursor.execute(query)	
						cursor.connection.commit()
						query  = "UPDATE queries SET no_subscribers = 0, timestamp = timestamp  WHERE query_id = %s and no_subscribers > 0;" % (queryId)
						cursor.execute(query)	
						cursor.connection.commit()	
				return resp 
			else:
				return errorResponse("Invalid ID", 404) 
		else:	
			return errorResponse("Invalid ID", 404)

		
@app.route('/post-reply', methods = ['POST'])
def postReply():

	val = checkRequestHeaders("true")
	if val is not None:		
		return val
	
	ip = request.remote_addr		
	if request.method == 'POST':
		queryId = request.form['query_id']
		queryStatus = request.form['query_status']
		if queryStatus is not None and queryId is not None:
			if queryStatus == "true":
				streamId = updateLiveStreamers(ip, queryId)
				if streamId > 0:
					return createPostReplyResponse(None,streamId)			
			
			else:	
				resp = Response(response=None, status=204, mimetype=None)
				return resp
					
		else:	
			return errorResponse()
			
		
		
def updateLiveStreamers(ip, queryId):
	rtsp_url = "rtsp://%s:1234"  % (ip)
	# add stream id to generate unique url
	uuidVal = uuid.uuid4()
	rtmp_url = "rtmp://%s:1935/mytv/%s" % (STREAM_SERVER, uuidVal)
	print rtsp_url
	print rtmp_url
	cursor = mysql.connect().cursor()
	query = "UPDATE queries SET no_active_streamers = no_active_streamers+1, timestamp = timestamp Where query_id = '" + queryId + "';"
	cursor.execute(query)	
	cursor.connection.commit()
	if cursor.rowcount == 1: 
		#start FFMPEG
		pid = initiateFFMPEG(rtsp_url, rtmp_url, uuidVal)
		#pid = 123456
		query = "INSERT into live_streams(query_id, rtmp_url, rtsp_url, is_stream_active, pid) VALUES('%s','%s','%s','%s', '%s');" % 			(queryId, rtmp_url, rtsp_url, 1, pid)
		cursor.execute(query)
    		cursor.connection.commit()
    		query  = "SELECT stream_id from live_streams where query_id = %s and pid = %s ORDER BY stream_id LIMIT 1" % (queryId, pid)
    		cursor.execute(query)
    		data = cursor.fetchone()
    		return data[0]
		
	cursor.close()
	return "0"

@app.route('/get-queries', methods = ['GET'])
def getQueries():
	
 	val = checkRequestHeaders()
 	if val is not None:		
		return val
		
	cursor = mysql.connect().cursor()
	#print "SELECT * from queries where TIME_TO_SEC(timediff(now(),timestamp))<=%s ORDER BY timestamp DESC;"
	cursor.execute("SELECT * from queries where TIME_TO_SEC(timediff(now(),timestamp))<=%s ORDER BY timestamp DESC;" %(30))
	queryList = generateJsonForQueries(cursor)    	
	if queryList:
		#resp = Response(json.dumps(queryList), status=200, mimetype='application/json')
		resp = jsonify({"queries": queryList, "count": len(queryList), "status": 200})
		resp.status_code = 200
		resp.mimetype = "application/json"		
		return resp
	else:
		data = 	{"message":"list is empty", "status":200, "count" : 0}
		resp = jsonify(data)
		resp.status_code = 200;
		resp.mimetype = "application/json"	     	
		#resp = Response(json.dumps(data), status=200, mimetype='application/json')
		return resp
		

def generateJsonForQueries(data, isSubmit=None):
	contentList = []
	desc = data.description
	for value in data:
            objectDict = collections.OrderedDict()
	    for (name, tupleValue) in zip(desc,value):
		    objectDict[name[0]] = tupleValue
		    
	    if isSubmit:
		    objectDict["stream_url"] = url_for('stream', stream_id=objectDict['stream_id'])
	    contentList.append(objectDict)

	return contentList		
	
def killFFMPEG(pid):
	os.kill(int(pid), 9)

def call_command(command):
	return    subprocess.Popen(command.split(' ')).pid
		
def initiateFFMPEG(rtsp_url, rtmp_url, uuidVal):
	#test =  "avconv -i %s -ar 44100 -r 30 -vsync cfr -q 30 -fflags nobuffer -f flv %s" % (rtsp_url, rtmp_url)
	#print test
	rtmp_url = "rtmp://%s:1935/mytv/%s" % (STREAM_SERVER_IP, uuidVal)
	return call_command("avconv -i %s -ar 44100 -r 30 -vsync cfr -fflags nobuffer -f flv %s" % (rtsp_url, rtmp_url))
		
@app.route('/stream/<int:stream_id>')
def stream(stream_id, rtmp_url=None, rtsp_url=None):
	#ip = request.remote_addr
	#ip = "128.237.209.240:1234"
	#call_command("avconv -i %s -ar 44100 -f flv %s" % (rtsp_url, rtmp_url))
	#call_command("avconv -i rtsp://%s -ar 44100 -r 30 -vsync cfr -q 30 -fflags nobuffer -f flv rtmp://%s:1935/mytv/vinay-phone" % (ip, STREAM_SERVER))
	if 'logged_in' in session and session.get('logged_in'):
		queryString  = "SELECT rtmp_url from live_streams WHERE stream_id=%d" % (stream_id)
		cursor = mysql.connect().cursor()
		cursor.execute(queryString)
		data  = cursor.fetchone();
		#print data
		if data is not None:
			rtmp_url = data[0]
			streamName = rtmp_url[rtmp_url.rfind("/")+1:]
			rtmp_url = rtmp_url[0:rtmp_url.rfind("/")+1]
			#print "stream name", streamName
			#print "rtmp ",rtmp_url
			#return render_template('index.html', stream_name="vinay-phone", stream_url="rtmp://%s:1935/mytv/" % (STREAM_SERVER))
			#return render_template('index.html', stream_name=streamName, stream_url=rtmp_url)
			return render_template('jwplayback.html', stream_name=streamName, stream_url=rtmp_url)
		else:
			return redirect(url_for('showHome')) 

        else:
                return render_template('login.html')

@app.route('/logout')
def logout():
    session.pop('logged_in', None)
    flash('You were logged out')
    return redirect(url_for('showHome')) 
	
if __name__ == '__main__':
    app.run(host='0.0.0.0')

