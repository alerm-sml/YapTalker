YapTalker
=========
[![Build Status](https://travis-ci.org/djkovrik/YapTalker.svg?branch=develop)](https://travis-ci.org/djkovrik/YapTalker/builds)
![minSdk](https://img.shields.io/badge/minSdk-19-green.svg)
![Last Commit](https://img.shields.io/github/last-commit/djkovrik/yaptalker/develop.svg)
![Current Release](https://img.shields.io/github/release/djkovrik/yaptalker/all.svg)


Unofficial open source Android client for yaplakal.com.

Keep in mind that it is still under development so bugs are possible.

Features
--------
With this app you can:
* Read news, forums and topics
* Customize main news page to filter news with chosen categories
* Browse active topics forum section
* Browse user profiles
* Browse, save and share images
* Watch videos (including yapfiles and embedded video clips from vk.com, coub etc.)
* Manage bookmarks
* Sign In and post messages
* Change user posts and topics rating

Build Notes
-----------
* To build the app you must add these lines into your local *gradle.properties* file (use actual values or dummy data):
```groovy
VK_ACCESS_TOKEN="Access token"
RELEASE_STORE_FILE="File path"
RELEASE_STORE_PASSWORD="Store password"
RELEASE_KEY_ALIAS="Key alias"
RELEASE_KEY_PASSWORD="Key password"
```
* The app uses access token to download vk.com video thumbnails, you can obtain a new token [here](https://vk.com/dev/access_token)


Download
--------
[![4pda](https://github.com/djkovrik/YapTalker/blob/master/graphics/4pda.png)](http://4pda.ru/forum/index.php?showtopic=881650)