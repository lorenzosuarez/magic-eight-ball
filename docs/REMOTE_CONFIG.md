# What can be changed from Firebase

Everything here is a Remote Config parameter of the project. A change reaches a phone that has the app open within moments, and any other the next time it fetches (at most about an hour). Nothing here needs a release. Whatever is missing or wrong falls back to what the app ships with: a slip in the console cannot break the app.

## Switches and numbers

| Parameter | Type | What it does |
|---|---|---|
| `gemini_enabled` | Boolean | AI answers on or off for everyone. |
| `gemini_api_key` | String | The key AI answers are asked with. |
| `gemini_use_paid_key`, `gemini_paid_api_key` | Boolean, String | When the switch is on and the paid key is there, it is used instead. |
| `gemini_model` | String | The model asked. Blank or absent: the app's own choice. |
| `gemini_daily_limit` | Number | AI answers one device may ask for in a day. |
| `ai_remark_enabled` | Boolean | Whether a second line may come under an answer to a spoken question. |
| `styles_featured` | String | Styles shown first in Options, by name in lower case, comma separated. |
| `styles_hidden` | String | Styles not offered at all, same format. |
| `ads_enabled` | Boolean | Ads on or off. |
| `ads_min_answers`, `ads_min_interval_seconds` | Number | How many answers and how much time between two ads. |
| `rewarded_ai_enabled`, `rewarded_ai_answers` | Boolean, Number | Whether an ad can be watched for more AI answers, and how many it gives. |
| `daily_verdict_enabled`, `daily_verdict_hour` | Boolean, Number | The day's verdict notification, and the hour it comes at (0 to 23, device time). |
| `notifications_ask_after_days` | Number | Days of use before the app asks to send notifications. |
| `streak_min_days_shown` | Number | The shortest run of days the app mentions. |
| `ball_sway_enabled` | Boolean | Whether the ball leans with the phone. |

## `answers_json`: the answers the app keeps

The lines used when the AI is off, away or used up. Same shape as `core/data/src/main/assets/magic_8_ball_responses.json`: a language, then a style, then the list. A list given here takes the place of the app's list for that language and style; the rest stay.

```json
{ "es": { "pirate": ["Sí. La mitad es mía.", "No. A fregar la cubierta."] } }
```

Each line: at most 28 characters and 6 words, so it fits the ball. The Classic style's twenty answers are never replaced.

## `characters_json`: who the characters are

What the AI is told about each character, and how an answer is asked for. Any part may be given and nothing else needs to be there.

```json
{
  "rules": {
    "form": "Give that answer as only this character would. Three to five words, six at the very most."
  },
  "characters": {
    "pirate": {
      "who": "A weathered pirate captain who has taken the asker on as the newest deckhand.",
      "wants": "Loyalty to the crew, and a cut of whatever the asker gains.",
      "yes": "An order to set sail at once, and a reminder of the captain's share.",
      "no": "A refusal with a cartoon threat: scrubbing the deck, or the plank.",
      "dodge": "Fog on the spyglass; or the parrot must be consulted first.",
      "habits": "Gruff, hearty, superstitious.",
      "never": "Modern words, or any real violence.",
      "angles": ["the captain wants a cut", "they owe the crew rum", "it suspects a mutiny"],
      "lines": {
        "es": {
          "yes": ["¡Zarpa ya! La mitad es mía.", "Sí. Y me debes ron."],
          "no": ["¡No! A fregar la cubierta.", "Jamás, grumete."],
          "maybe": ["Niebla. Tú sigue remando.", "Pregúntale al loro."],
          "remarks": [{ "q": "¿Pido un aumento?", "a": "Pídelo, y la mitad del aumento es del capitán." }]
        }
      }
    }
  }
}
```

**A character** is named by its style in lower case (`sarcastic`, `mystical`, `grandma`, `pirate`, `philosopher`, `coach`, `lovestruck`, `drama`, `robot`, `detective`, `conspiracy`, `cat`, `zoomer`, `villain`, `poet`, `astro`, `therapist`, `dog`). Classic has no character and takes none.

- `who`, `wants`, `yes`, `no`, `dodge`, `habits`, `never`: a sentence or two each, in English, at most 600 characters. Write what the character does, not its catchwords in quotes: a quoted English word ends up in a Spanish answer.
- `angles`: what the character may bring up when nothing was asked. At least 3, each at most 120 characters.
- `lines`, by language (`en`, `es`, `pt`): lines the character has said, shown to the AI two at a time as its voice. `yes`, `no` and `maybe` need at least 2 lines each, of at most 6 words and 40 characters. `remarks` pair a question with the comeback to it, the comeback of at most 12 words.

**Rules** say how an answer is written, whoever writes it: `craft`, `form`, `examples`, `remark`, `question_craft`, each at most 1200 characters. Their texts as shipped are in `core/domain/.../policy/CharacterBook.kt`. What guards against the question, what a toy does not answer and the limit of six words are not among them and cannot be changed from here.

A value that breaks one of these limits is left out and the app's own is used in its place. A file that is not valid JSON is ignored whole.

## What still needs a release

A new style (its name in three languages, its colour and what the ball wears), the texts of the screens, and anything about how the app looks.
