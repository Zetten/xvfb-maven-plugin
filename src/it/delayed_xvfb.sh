#!/bin/bash

sleep $1
shift

Xvfb $@
