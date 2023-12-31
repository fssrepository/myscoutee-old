#!/bin/bash

for pid in $(ps -eo %cpu,pid,cmd --sort=-%cpu | awk '$1 >= 40 {print $2}'); do kill -9 $pid; done

