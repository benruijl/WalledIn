#!/bin/bash

java -classpath .:*:lib/*  -Djava.library.path=lib/native/mac walledin.game.ClientLogicManager
