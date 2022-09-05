# Device Data API

## Building

`sbt assembly` will build a jar file under `target/scala-2.13`

## Versions

I'm using:

- java zulu-8.60.0.21
- sbt 1.4.1

## API Interface

Three Routes:

### GET /healthheck

Will always return a `200` with a string to confirm the server is accepting traffic

### GET /<device id>

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

### POST /<device id>

Send it a json payload as described in the document, it will process the payload.

if your payload does not match what is expected it will return a `400` response

example:

```
curl http://localhost:8080/device-x -H 'content-type: application/json' -d '{"id":"device-x","readings":[{"timestamp":"2023-05-29T17:08:15+01:00", "count":4}]}'
```