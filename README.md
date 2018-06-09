# Demonstration of the issues we are having with Comet

1. Just run `sh bootstrap.sh`

This will start all containers and the Kotlin coordinator that will register entities in Orion, simulate entity events, and try to gather aggregations from Comet.

You should see some Comet error messages such as:

```
sth_1          | time=2018-06-09T11:04:02.626Z | lvl=WARN | corr=n/a | trans=n/a | op=OPER_STH_DB_LOG | from=n/a | srv=n/a | subsrv=n/a | comp=STH | msg=The size in bytes of the namespace for storing the aggregated data ("sth_sensei_service" plus "sth_/sensei,/sensei,/sensei,/sensei,/sensei,/sensei,/sensei,/sensei,/sensei,/sensei_PersonDetection_PersonDetection.aggr", 138 bytes) is bigger than 120 bytes
```

```
sth_1          | time=2018-06-09T11:04:12.870Z | lvl=ERROR | corr=d78056a4-6bd4-11e8-97dd-0242ac120005 | trans=745ad73e-ebd0-49a4-b843-261981c8f9b2 | op=OPER_STH_POST | from=n/a | srv=sensei_service | subsrv=/sensei | comp=STH | msg=Error when getting the raw data collection for storing:MongoError: a collection 'sth_sensei_service.sth_/sensei_PersonDetection_PersonDetection' already exists
```

And the Kotlin coordinator complaining the following:

```
demo_1         | Requesting aggregation to Comet:
demo_1         | {"contextResponses":[{"contextElement":{"attributes":[{"name":"positionX","values":[]}],"id":"PersonDetection","isPattern":false,"type":"PersonDetection"},"statusCode":{"code":"200","reasonPhrase":"OK"}}]}
demo_1         |
demo_1         |        Comet seems to be sending an empty "values" array. What is going on?
demo_1         |
```