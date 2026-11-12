#!/usr/bin/env python3
"""Builds the app's public site into site/public.

The site is the privacy policy in the three languages the app speaks, and a front page for each.
The policy has one source: the Markdown files under docs/. This turns them into pages, so that
what is published can never drift from what is in the repository. It runs before every deploy
(see firebase.json) and needs nothing but Python.
"""

import html
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "site" / "public"
STATIC = ROOT / "site" / "static"
BASE_URL = "https://magic-ai-8-ball.web.app"
PLAY_URL = "https://play.google.com/store/apps/details?id=com.lorenzosuarez.magiceightball"

# English is at the root; the other languages each have a folder of their own.
LANGUAGES = {
    "en": {
        "html_lang": "en",
        "prefix": "",
        "policy": "PRIVACY_POLICY.md",
        "name": "English",
        "tagline": "A Magic 8 Ball with character. Tap, shake or ask out loud.",
        "description": "Magic AI Ball: a Magic 8 Ball with character, for Android phones and Wear OS.",
        "get": "Get it on Google Play",
        "privacy": "Privacy policy",
        "home": "Home",
        "language": "Language",
    },
    "es": {
        "html_lang": "es",
        "prefix": "es/",
        "policy": "PRIVACY_POLICY.es.md",
        "name": "Español",
        "tagline": "Una bola 8 mágica con carácter. Toca, agita o pregunta en voz alta.",
        "description": "Magic AI Ball: una bola 8 mágica con carácter, para teléfonos Android y Wear OS.",
        "get": "Disponible en Google Play",
        "privacy": "Política de privacidad",
        "home": "Inicio",
        "language": "Idioma",
    },
    "pt": {
        "html_lang": "pt-BR",
        "prefix": "pt/",
        "policy": "PRIVACY_POLICY.pt.md",
        "name": "Português",
        "tagline": "Uma bola 8 mágica com personalidade. Toque, agite ou pergunte em voz alta.",
        "description": "Magic AI Ball: uma bola 8 mágica com personalidade, para celulares Android e Wear OS.",
        "get": "Disponível no Google Play",
        "privacy": "Política de privacidade",
        "home": "Início",
        "language": "Idioma",
    },
}


def inline(text: str) -> str:
    """The marks the policy uses inside a line: code, bold and links. Everything else is text."""
    text = html.escape(text, quote=False)
    text = re.sub(r"`([^`]+)`", r"<code>\1</code>", text)
    text = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", text)
    return re.sub(
        r"\[([^\]]+)\]\(([^)\s]+)\)",
        lambda link: f'<a href="{html.escape(link.group(2))}" rel="noopener">{link.group(1)}</a>',
        text,
    )


def markdown(source: str) -> tuple[str, str]:
    """The policy's title and its body as HTML. It knows the few kinds of line the policy has."""
    title, body, items = "", [], []

    def close_list() -> None:
        if items:
            body.append("<ul>\n" + "\n".join(f"<li>{item}</li>" for item in items) + "\n</ul>")
            items.clear()

    for line in source.splitlines():
        if line.startswith("- "):
            items.append(inline(line[2:]))
            continue
        close_list()
        if line.startswith("# "):
            title = line[2:].strip()
        elif line.startswith("## "):
            body.append(f"<h2>{inline(line[3:].strip())}</h2>")
        elif line.strip():
            if line.startswith(("#", "|", ">", "    ")) or re.match(r"\d+\. ", line):
                raise ValueError(f"A kind of line this does not know how to publish: {line[:60]!r}")
            body.append(f"<p>{inline(line.strip())}</p>")
    close_list()
    if not title:
        raise ValueError("The policy has no title")
    return title, "\n".join(body)


def url(code: str, page: str) -> str:
    """Where a page is served: no extension, and the front page is the folder itself."""
    path = (LANGUAGES[code]["prefix"] + ("" if page == "index" else page)).rstrip("/")
    return "/" + path


def page(code: str, name: str, title: str, description: str, main: str, main_class: str) -> str:
    words = LANGUAGES[code]
    alternates = "\n".join(
        f'<link rel="alternate" hreflang="{other["html_lang"]}" href="{BASE_URL}{url(key, name)}">'
        for key, other in LANGUAGES.items()
    )
    switch = "\n".join(
        f'<a href="{url(key, name)}" lang="{other["html_lang"]}" hreflang="{other["html_lang"]}"'
        + (' aria-current="page"' if key == code else "")
        + f'>{other["name"]}</a>'
        for key, other in LANGUAGES.items()
    )
    return f"""<!doctype html>
<html lang="{words["html_lang"]}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="color-scheme" content="light dark">
<title>{html.escape(title)}</title>
<meta name="description" content="{html.escape(description)}">
<link rel="canonical" href="{BASE_URL}{url(code, name)}">
{alternates}
<link rel="alternate" hreflang="x-default" href="{BASE_URL}{url("en", name)}">
<link rel="icon" href="/icon.svg" type="image/svg+xml">
<link rel="stylesheet" href="/style.css">
</head>
<body>
<header>
<a class="brand" href="{url(code, "index")}" aria-label="Magic AI Ball: {html.escape(words["home"])}">
<img src="/icon.svg" alt="" width="28" height="28">Magic AI Ball</a>
<nav aria-label="{html.escape(words["language"])}">
{switch}
</nav>
</header>
<main class="{main_class}">
{main}
</main>
<footer>
<a href="{url(code, "privacy")}">{html.escape(words["privacy"])}</a>
<a href="{PLAY_URL}" rel="noopener">Google Play</a>
</footer>
</body>
</html>
"""


def write(path: str, content: str) -> None:
    target = OUT / path
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(content, encoding="utf-8")


def build() -> None:
    if OUT.exists():
        shutil.rmtree(OUT)
    shutil.copytree(STATIC, OUT)

    for code, words in LANGUAGES.items():
        title, body = markdown((ROOT / "docs" / words["policy"]).read_text(encoding="utf-8"))
        write(
            words["prefix"] + "privacy.html",
            page(code, "privacy", title, title, f"<h1>{inline(title)}</h1>\n{body}", "text"),
        )
        front = (
            '<img class="ball" src="/icon.svg" alt="" width="144" height="144">\n'
            "<h1>Magic AI Ball</h1>\n"
            f'<p class="tagline">{html.escape(words["tagline"])}</p>\n'
            '<p class="actions">\n'
            f'<a class="primary" href="{PLAY_URL}" rel="noopener">{html.escape(words["get"])}</a>\n'
            f'<a href="{url(code, "privacy")}">{html.escape(words["privacy"])}</a>\n'
            "</p>"
        )
        write(words["prefix"] + "index.html", page(code, "index", "Magic AI Ball", words["description"], front, "front"))

    # A page that is not there: said in the three languages, since the address does not tell which.
    missing = "\n".join(
        f'<p lang="{words["html_lang"]}"><a href="{url(code, "index")}">{html.escape(words["home"])}</a></p>'
        for code, words in LANGUAGES.items()
    )
    write("404.html", page("en", "index", "Magic AI Ball", "Page not found", f"<h1>404</h1>\n{missing}", "front"))

    pages = [url(code, name) for code in LANGUAGES for name in ("index", "privacy")]
    write("robots.txt", f"User-agent: *\nAllow: /\nSitemap: {BASE_URL}/sitemap.xml\n")
    write(
        "sitemap.xml",
        '<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
        + "".join(f"<url><loc>{BASE_URL}{path}</loc></url>\n" for path in pages)
        + "</urlset>\n",
    )
    print(f"Built {len(pages)} pages into {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    build()
