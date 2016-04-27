# App Engine Firebase Event Proxy


## Configuration

### Java Firebase Event Proxy
Firebase Secret
Put your Firebase secret in the file:
gae-firebase-event-proxy/src/main/webapp/firebase-secret.properties
```
firebaseSecret=<Your Firebase secret>
```

* Billing must be enabled from Cloud console.
* Manual scaling should turned on and configured to 1 instance in appengine-web.xml

## Deploy

### Java Firebase Event Proxy
```
cd gae-firebase-event-proxy
mvn appengine:upload
```

### Python App Engine Listener
```
appcfg.py -A <your app id> -V v1 update gae-firebase-listener-python
```
