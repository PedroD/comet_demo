import khttp.get
import khttp.patch
import khttp.post
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.reflect.KClass

const val SERVICE_NAME = "sensei_service"
const val SERVICE_PATH = "/sensei"

val EntityByType = HashMap<String, KClass<out EntityMetadata>>()

fun main(args: Array<String>) {
    println("Giving some time for all containers to start...")
    Thread.sleep(15000)

    setEntityTypes()
    val entities = parseEntities()

    registerEntitiesInOrion(entities)
    subscribeCometToOrionEntities()

    Thread.sleep(10000)

    while (true) {
        emitMockPersonDetection()
        Thread.sleep(2000)
        getAveragePosition()
        Thread.sleep(2000)
    }
}

fun getAveragePosition() {
    val headers = mapOf(Pair("FIWARE-ServicePath", SERVICE_PATH), Pair("FIWARE-Service", SERVICE_NAME))
    val result = get("http://sth:8666/STH/v1/contextEntities/type/PersonDetection/id/PersonDetection/attributes/positionX?aggrMethod=sum&aggrPeriod=second&dateFrom=2016-02-01T00:00:00.000Z&dateTo=2019-01-01T23:59:59.999Z",
            headers = headers)

    println("Requesting aggregation to Comet:\n${result.text}\n")

    if(result.text.contains("\"values\":[]")) {
        println("\tComet seems to be sending an empty \"values\" array. What is going on?\n")
    }
}

fun emitMockPersonDetection() {
    val json = JSONObject("""
        {
            "tagId": {
              "type": "Integer",
              "value": -1
            },
            "sectorId": {
              "type": "Integer",
              "value": -1
            },
            "positionX": {
              "type": "Float",
              "value": ${Math.random() * 100}
            },
            "positionY": {
              "type": "Float",
              "value": ${Math.random() * 100}
            },
            "ts": {
              "type": "Timestamp",
              "value": "${DateTime.now()}"
            }
        }
        """)

    val headers = mapOf(Pair("FIWARE-ServicePath", SERVICE_PATH), Pair("FIWARE-Service", SERVICE_NAME))
    val res = patch("http://orion:1026/v2/entities/PersonDetection/attrs", json = json,
            headers = headers)

    println("Emitting Person Detection Events (http://orion:1026/v2/entities/PersonDetection/attrs):\n$json")
    println("With headers:\n${headers.toList().joinToString("\n") { "    " + it.first + ": " + it.second }}\n")

    if (res.statusCode != 204) throw Exception("Error while emitting PersonDetection events: ${res.statusCode}")

    Thread.sleep(2000)
}

fun subscribeCometToOrionEntities() {
    val sthSubscription =
            """
    {        
          "entities": [
        {
            "type": "PersonDetection",
            "isPattern": "false",
            "id": "PersonDetection"
        }
      ],
      "attributes": [ "ts" ],
      "reference": "http://sth:8666/notify"
    }
"""

    val headers = mapOf(Pair("FIWARE-ServicePath", SERVICE_PATH), Pair("FIWARE-Service", SERVICE_NAME))
    val res = post("http://orion:1026/v1/subscribeContext", json = JSONObject(sthSubscription),
            headers = headers)

    println("Subscribing STH to Orion entities (http://orion:1026/v1/subscribeContext):\n$sthSubscription")
    println("With headers:\n${headers.toList().joinToString("\n") { "    " + it.first + ": " + it.second }}")

    if (res.statusCode != 200) throw Exception("Error while subscribing Comet: ${res.statusCode}")
}


fun registerEntitiesInOrion(entities: List<EntityMetadata>) {
    val entitiesJson = JSONArray()
    entities.forEach { e ->
        entitiesJson.put(e.json)
    }

    val json = JSONObject().put("actionType", "APPEND").put("entities", entitiesJson)
    val headers = mapOf(Pair("FIWARE-ServicePath", SERVICE_PATH), Pair("FIWARE-Service", SERVICE_NAME))
    val res = post("http://orion:1026/v2/op/update",
            json = json,
            headers = headers)

    println("Registering entities in Orion (http://orion:1026/v2/op/update):\n${json.toString(4)}")
    println("With headers:\n${headers.toList().joinToString("\n") { "    " + it.first + ": " + it.second }}")

    if (res.statusCode != 204) throw Exception("Error while registering entities in Orion: ${res.statusCode}")
}

fun parseEntities(): List<EntityMetadata> {
    val json = JSONArray(File("entities.json").readText(charset("UTF-8")))
    val entities = LinkedList<EntityMetadata>()

    json.forEach {
        val type = (it as JSONObject).getString("type")
        entities.add(EntityByType[type]!!.constructors.first().call(it))
    }

    return entities
}

fun setEntityTypes() {
    EntityByType["Stores"] = Stores::class
    EntityByType["Sectors"] = Sectors::class
    EntityByType["Areas"] = Areas::class

    EntityByType["PersonDetection"] = PersonDetection::class
    EntityByType["PersonCounter"] = PersonCounter::class
    EntityByType["CounterOccupancy"] = CounterOccupancy::class

    EntityByType["CellDetection"] = CellDetection::class
    EntityByType["AreaVisit"] = AreaVisit::class
    EntityByType["StoreVisit"] = StoreVisit::class
    EntityByType["StoreOccupancy"] = StoreOccupancy::class
}