#!/bin/bash

java -classpath WalledInJava-0.1.jar:lib/*  -Djava.library.path=lib/native/linux64 walledin.game.network.client.Client 
