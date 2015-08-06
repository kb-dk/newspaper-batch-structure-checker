#!/usr/bin/env bash
# The purpose of this script is simply to generate a quick list of "checks" (and TODOs) from
# demands.sch, the checks being all the different things that demands.sch asserts or reports about
# the input batch structure in case of an error.

# Extract checks
grep 'Check:' demands.sch | sed -r 's/^(\ |\t)*<!--\ ?//g' | sed -r 's/\ *(Check)/\1/g' | sed -r 's/\ *-->//g'

echo

# Extract TODOs
grep TODO demands.sch | sed -r 's/^(\ |\t)*<!--\ ?//g' | sed -r 's/\ *(TODO)/\1/g' | sed -r 's/\ *-->//g'
