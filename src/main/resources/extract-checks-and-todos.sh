#!/usr/bin/env bash

# Extract checks
grep 'Check:' demands.sch | sed -r 's/^(\ |\t)*<!--\ ?//g' | sed -r 's/\ *(Check)/\1/g' | sed -r 's/\ *-->//g'

echo

# Extract TODOs
grep TODO demands.sch | sed -r 's/^(\ |\t)*<!--\ ?//g' | sed -r 's/\ *(TODO)/\1/g' | sed -r 's/\ *-->//g'
