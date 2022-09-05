# Device Data API

## Building & Running

`sbt assembly` will build a jar file under `target/scala-2.13`

[I'll also attach it as a github release](https://github.com/nickfun/device-data-api/releases)

You can set the port to bind to with the environment variable `PORT` 

if you downloaded the jar from github, run with `java -jar server.jar`

if you build it locally, run it with `java -jar target/scala-2.13/*.jar`

## Versions

I'm using:

- java zulu-8.60.0.21
- sbt 1.4.1

## API Interface

Three Routes:

### GET /healthheck

Will always return a `200` with a string to confirm the server is accepting traffic

### GET /{deviceId}

if the device has not sent data before, the response is a 404

if the device has sent data before, example response is:

```json
{
    "currentCount": 12,
    "mostRecentReading": {
        "count": 4,
        "timestamp": "2023-05-29T17:08:15+01:00"
    }
}
```

### POST /{deviceId}

Send it a json payload as described in the document, it will process the payload.

if your payload does not match what is expected it will return a `400` response

example:

```
curl http://localhost:8080/device-x -H 'content-type: application/json' -d '{"id":"device-x","readings":[{"timestamp":"2023-05-29T17:08:15+01:00", "count":4}]}'
```

## Things I would have like to done more

- testing of the http server routes
- real logging instead of println
- more useful response when sending an Incoming Payload