package com.example.fridgehelper.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationService @Inject constructor() {

    // lokalny słownik
    private val localDictionary = mapOf(
        "jabłko" to "apple", "jabłka" to "apple",
        "banan" to "banana", "banany" to "banana",
        "pomarańcza" to "orange", "pomarańcze" to "orange",
        "cytryna" to "lemon", "cytryny" to "lemon",
        "truskawka" to "strawberry", "truskawki" to "strawberry",
        "winogrona" to "grapes", "winogrono" to "grapes",
        "mango" to "mango", "arbuz" to "watermelon",
        "gruszka" to "pear", "gruszki" to "pear",
        "śliwka" to "plum", "śliwki" to "plum",
        "malina" to "raspberry", "maliny" to "raspberry",
        "borówka" to "blueberry", "borówki" to "blueberry",
        "kiwi" to "kiwi", "ananas" to "pineapple",
        "brzoskwinia" to "peach", "brzoskwinie" to "peach",

        "marchew" to "carrot", "marchewka" to "carrot",
        "ziemniak" to "potato", "ziemniaki" to "potato",
        "cebula" to "onion", "cebule" to "onion",
        "czosnek" to "garlic", "pomidor" to "tomato",
        "pomidory" to "tomato", "ogórek" to "cucumber",
        "ogórki" to "cucumber", "papryka" to "pepper",
        "szpinak" to "spinach", "sałata" to "lettuce",
        "brokuł" to "broccoli", "brokuły" to "broccoli",
        "kapusta" to "cabbage", "kalafior" to "cauliflower",
        "cukinia" to "zucchini", "bakłażan" to "eggplant",
        "por" to "leek", "seler" to "celery",
        "burak" to "beet", "buraki" to "beet",
        "groszek" to "peas", "fasola" to "beans",
        "kukurydza" to "corn",

        "mleko" to "milk", "masło" to "butter",
        "ser" to "cheese", "jajko" to "egg",
        "jajka" to "eggs", "jogurt" to "yogurt",
        "śmietana" to "cream", "śmietanka" to "cream",
        "twaróg" to "cottage cheese", "kefir" to "kefir",

        "kurczak" to "chicken", "wołowina" to "beef",
        "wieprzowina" to "pork", "indyk" to "turkey",
        "łosoś" to "salmon", "tuńczyk" to "tuna",
        "krewetki" to "shrimp", "dorsz" to "cod",
        "boczek" to "bacon", "szynka" to "ham",
        "kiełbasa" to "sausage",

        "chleb" to "bread", "mąka" to "flour",
        "ryż" to "rice", "makaron" to "pasta",
        "płatki owsiane" to "oats", "kasza" to "groats",

        "cukier" to "sugar", "sól" to "salt",
        "olej" to "oil", "oliwa" to "olive oil",
        "miód" to "honey", "dżem" to "jam",
        "czekolada" to "chocolate", "kakao" to "cocoa",
        "ketchup" to "ketchup", "musztarda" to "mustard",
        "majonez" to "mayonnaise"
    )

    suspend fun translateToEnglish(polishName: String): String {
        val lower = polishName.lowercase().trim()
        //najpierw sprawdza sie lokalny słownik aplikacji
        localDictionary[lower]?.let { return it }

        //jeśli nie ma — MyMemory API
        return try {
            translateViaApi(polishName)
        } catch (e: Exception) {
            Log.w("TranslationService", "Tłumaczenie nieudane dla: $polishName", e)
            polishName
        }
    }

    private suspend fun translateViaApi(text: String): String =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url = "https://api.mymemory.translated.net/get?q=$encoded&langpair=pl|en"
            val response = URL(url).readText()
            val json = JSONObject(response)
            val translated = json
                .getJSONObject("responseData")
                .getString("translatedText")
            translated.lowercase().trim()
        }
}