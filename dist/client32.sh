#!/bin/bash

java -classpath *:lib/*  -Djava.library.path=lib/native/linux32 walledin.game.network.client.Client 
