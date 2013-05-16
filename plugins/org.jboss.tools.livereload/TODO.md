# Status

## Directory mode:

- Direct access (file://) works
- Implement server/proxy access to static files, with security concerns

## Server mode:
- Basic implementation works
- Missing: listeners when a server is created/deleted, and then, when a server is started/stopped/restarted
- Then, filtering messages to send to the browser given their current location (selecting host/port), to avoid sending notification to browsers "connected" to another server
- Implement the proxy mode to inject scripts.



