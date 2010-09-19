#!/bin/bash

java -classpath .:*:lib/*  -Djava.library.path=lib/native/linux64 walledin.game.ClientLogicManager
