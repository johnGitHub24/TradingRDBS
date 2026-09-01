#!/usr/bin/env bash
# TradingRDBS - portable env
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PORTABLE="$SCRIPT_DIR/portable-env.sh"
if [[ ! -f "$PORTABLE" ]]; then
  WALK="$(cd "$SCRIPT_DIR/.." && pwd)"
  for _ in 1 2 3 4 5 6; do
    EOS="$WALK/EngineeringOS/eos-minimal/hooks/portable-env.sh"
    if [[ -f "$EOS" ]]; then PORTABLE="$EOS"; break; fi
    PARENT="$(dirname "$WALK")"
    [[ "$PARENT" == "$WALK" ]] && break
    WALK="$PARENT"
  done
fi
if [[ -f "$PORTABLE" ]]; then
  # shellcheck source=/dev/null
  source "$PORTABLE"
else
  echo "WARN: portable-env.sh not found" >&2
fi
