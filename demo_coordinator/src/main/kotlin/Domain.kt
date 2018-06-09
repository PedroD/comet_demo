import org.json.JSONObject

abstract class EntityMetadata(val json: JSONObject)

class Stores(json: JSONObject) : EntityMetadata(json)
class Sectors(json: JSONObject) : EntityMetadata(json)
class Areas(json: JSONObject) : EntityMetadata(json)

class PersonDetection(json: JSONObject) : EntityMetadata(json)
class PersonCounter(json: JSONObject) : EntityMetadata(json)
class CounterOccupancy(json: JSONObject) : EntityMetadata(json)

class CellDetection(json: JSONObject) : EntityMetadata(json)
class AreaVisit(json: JSONObject) : EntityMetadata(json)
class StoreVisit(json: JSONObject) : EntityMetadata(json)
class StoreOccupancy(json: JSONObject) : EntityMetadata(json)