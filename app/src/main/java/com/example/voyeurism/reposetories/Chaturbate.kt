package com.example.voyeurism.reposetories

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.voyeurism.activitys.MainActivity
import com.example.voyeurism.models.ChaturbateModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

const val urlSufix:String = "page="
const val urlDetails:String = "https://chaturbate.com/"
const val jsonUrl:String = "https://chaturbate.com/affiliates/api/onlinerooms/?format=json&wm=P3YWn"

class Chaturbate {

    interface ApiService {
        @GET("/affiliates/api/onlinerooms/?format=json&wm=P3YWn")
        suspend fun getModels(): Response<List<ChaturbateModel>>
    }

    @SuppressLint("SuspiciousIndentation")
    suspend fun parseJson():MutableList<ChaturbateModel> {
        val models:MutableList<ChaturbateModel> = mutableListOf()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://chaturbate.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(ApiService::class.java)
        val response = service.getModels()
        if (response.isSuccessful) {
            val items = response.body()
            if (items != null) {
                try {
                    for (item in items) {
                        val username: String = item.username
                        val roomSubject: String = item.room_subject
                        val gender: String = item.gender
                        val imageUrl: String = item.image_url
                        val age: String = item.age.toString()
                        val location: String = item.location
                        val new: Boolean = item.is_new
                        val hd: Boolean = item.is_hd
                        val numUsers: Int = item.num_users
                        val numFollower: Int = item.num_followers
                        val secondsOnline: Int = item.seconds_online
                        val tagsJSONArray = item.tags
                        val tags: ArrayList<String> = arrayListOf()
                        for (m in tagsJSONArray) {
                            tags.add(m.toString())
                            //  Log.d("HIER?!!!!M-> : ", m)
                        }
                        val link = "https://chaturbate.com/$username"
                        models.add(
                            ChaturbateModel(
                                gender,
                                location,
                                true,
                                username,
                                roomSubject,
                                tags,
                                new,
                                numUsers,
                                numFollower,
                                "",
                                "",
                                "",
                                hd,
                                age.toInt(),
                                secondsOnline,
                                imageUrl,
                                "",
                                "",
                                link,
                                "",
                                "",
                                "",
                                "",
                                false,
                                slug = false,
                                link = link
                            )
                        )
                    }
                } catch (e: HttpException) {
                    Log.e("Voyeurism ", "Error parsing " + e.message, e)
                }
            }
        }
        return models
    }

    fun parseJson(genderSelection:String):MutableList<ChaturbateModel> {7
         val listModels:MutableList<ChaturbateModel> = mutableListOf()
         val queue = Volley.newRequestQueue(MainActivity())
         val request = JsonArrayRequest(Request.Method.GET, "https://chaturbate.com/affiliates/api/onlinerooms/?format=json&wm=P3YWn", null, { response ->
             try { for (i in 0 until response.length()) {
                 val respObj = response.getJSONObject(i)
                 if (respObj.getString("gender") == genderSelection ){
                     val gender:String = respObj.getString("gender")
                     val name:String = respObj.getString("username")
                     val image:String = respObj.getString("image_url")
                     val location:String = respObj.getString("location")
                     val hd:Boolean = respObj.getString("is_hd").toBoolean()
                     val new:Boolean = respObj.getString("is_new").toBoolean()
                     val age:String = respObj.getString("age")
                     val description:String = respObj.getString("room_subject")
                     val views:String = respObj.getString("num_users")
                     //val tags:ArrayList<String> = respObj.getJSONArray("tags")
                     val link = "https://chaturbate.com/$name/"
                     listModels.add(ChaturbateModel(gender, location, false, name, description, arrayListOf(), new, 0, 0, "", "", "", hd, age.toInt(), 0, image, "", "", "","","","","",
                         recorded = true,
                         slug = true,
                         link))
                     Log.d("", "$name + $image + $hd + $age + $gender + $description + $location + $views + $link")
                 }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(MainActivity(), e.toString(), Toast.LENGTH_SHORT).show()
                Log.d("", e.printStackTrace().toString())
            }
        }, { error ->
            Toast.makeText(MainActivity(), "Fail to get response", Toast.LENGTH_SHORT).show()
            Log.d("", error.toString())
        })
        queue.add(request)
        return listModels
    }

    fun parseImage(url:String):String {
        var imageString = ""
        val html:String = jsoupToHtml(url)
        val reg = "<meta property=\"og:image\" content=\"([^\"\"]*)\" />".toRegex()
        reg.findAll(html).forEach {
            imageString = it.groupValues[0]
        }
        return imageString
    }

    fun parseVideo(url:String):String {
        val html:String = jsoupToHtml(url)
        val rexx = """http(s)?://([\w+?\.\w+])+([a-zA-Z0-9\~\!\@\#\$\%\^\&\*\(\)_\-\=\+\\\/\?\.\:\;\'\,]*)?.m3u8""".toRegex()
        val finds = rexx.findAll(html)
        finds.forEach {
            var rep: String = it.groupValues[0]
            rep = rep.replace("\\u0027", "'").replace("\\u0022", "\"").replace("\\u002D", "-").replace("\\u005C", "\"").replace("u003D", "=")
            //  Log.d("", rep)
            return rep
        }
        return ""
    }

    fun parseCams(url:String, pageCount:Int):MutableList<ChaturbateModel>{
        val listData = mutableListOf<ChaturbateModel>()
        val doc: Document = jsoupToDocument(url + urlSufix + pageCount)
        val tagName = doc.tagName("li")
        for (tag in tagName.getElementsByClass("room_list_room")){
            val name:String = tag.getElementsByClass("no_select").attr("href").replace("/", "")
            val image:String = tag.getElementsByClass("png").attr("src")
            val age:String = isINT(tag.getElementsByClass("age").text())
            val label:String = tag.getElementsByClass("thumbnail_label").text()
            val views:String = tag.getElementsByClass("cams").text()
            val descriptions:String = tag.getElementsByClass("subject").text()
            val link:String = "https://chaturbate.com" + tag.getElementsByClass("no_select").attr("href")
            // listData.add(ChaturbateModel(name,image,label,age,descriptions,views,link))
            Log.d("", name)
        }
        return listData
    }

    private fun isINT(value:String):String{
        val check = "[0-9]+".toRegex()
        return if(value.matches(check)){ value } else{""}
    }

    private fun jsoupToHtml(url:String): String{
        var html:String? = null
        try{
            html = Jsoup.connect(url).get().html()
        } catch (e: Exception) {
            Log.e("Voyeurism ", "Error parsing " + e.message, e)
        }
        return html!!
    }

    private fun jsoupToDocument(url:String): Document {
        var doc: Document? = Document(url)
        try{
            doc = Jsoup.connect(url).get()
        } catch (e: Exception) {
            Log.e("Voyeurism ", "Error parsing " + e.message, e)
        }
        return doc!!
    }

}