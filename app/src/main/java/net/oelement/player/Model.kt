package net.oelement.player

import org.json.JSONObject

class Model {
    lateinit var url:String
    lateinit var data:JSONObject
    constructor(url: String, data:JSONObject) {
        this.url = url
        this.data = data
    }

    constructor()
}