#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<'USAGE'
Usage:
  ./start-mcp.sh -amap -wecom -email
  ./start-mcp.sh -all

Options:
  -amap       Start mcp-server-amap    (port 9003)
  -wecom      Start mcp-server-wecom   (port 9002)
  -email      Start mcp-server-email   (port 9004)
  -bocha      Start mcp-server-bocha   (port 9005)
  -csdn       Start mcp-server-csdn    (port 9001)
  -all        Start all MCP servers
  -h, --help  Show this message
USAGE
}

module_for() {
  case "$1" in
    amap) echo "mcp-server-amap" ;;
    wecom) echo "mcp-server-wecom" ;;
    email) echo "mcp-server-email" ;;
    bocha) echo "mcp-server-bocha" ;;
    csdn) echo "mcp-server-csdn" ;;
    *) return 1 ;;
  esac
}

port_for() {
  case "$1" in
    amap) echo "9003" ;;
    wecom) echo "9002" ;;
    email) echo "9004" ;;
    bocha) echo "9005" ;;
    csdn) echo "9001" ;;
    *) return 1 ;;
  esac
}

is_running() {
  local pid="$1"
  kill -0 "$pid" >/dev/null 2>&1
}

kill_by_port() {
  local port="$1"
  local pids

  if ! command -v lsof >/dev/null 2>&1; then
    echo "[ERROR] lsof is required to detect occupied ports."
    exit 1
  fi

  pids="$(lsof -ti tcp:"$port" 2>/dev/null || true)"
  if [[ -z "$pids" ]]; then
    return 0
  fi

  echo "[INFO] Port $port is occupied. Stopping process(es): $pids"
  for pid in $pids; do
    kill "$pid" >/dev/null 2>&1 || true
  done

  sleep 1
  for pid in $pids; do
    if is_running "$pid"; then
      kill -9 "$pid" >/dev/null 2>&1 || true
    fi
  done
}

start_one() {
  local key="$1"
  local module
  local port
  module="$(module_for "$key")"
  port="$(port_for "$key")"

  local module_dir="$SCRIPT_DIR/$module"

  if [[ ! -d "$module_dir" ]]; then
    echo "[ERROR] Module directory not found: $module_dir"
    return 1
  fi

  kill_by_port "$port"

  echo "[START] $module (port=$port)"
  (
    cd "$module_dir"
    nohup mvn -q -DskipTests spring-boot:run >/dev/null 2>&1 &
    echo "[OK]    $module started (pid=$!, port=$port)"
  )
}

if [[ $# -eq 0 ]]; then
  usage
  exit 1
fi

selected=()
for arg in "$@"; do
  case "$arg" in
    -amap) selected+=("amap") ;;
    -wecom) selected+=("wecom") ;;
    -email) selected+=("email") ;;
    -bocha) selected+=("bocha") ;;
    -csdn) selected+=("csdn") ;;
    -all) selected=("amap" "wecom" "email" "bocha" "csdn") ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[ERROR] Unknown option: $arg"
      usage
      exit 1
      ;;
  esac
done

if [[ ${#selected[@]} -eq 0 ]]; then
  echo "[ERROR] No service selected"
  usage
  exit 1
fi

uniq_selected=()
for s in "${selected[@]-}"; do
  exists=0
  for u in "${uniq_selected[@]-}"; do
    if [[ "$u" == "$s" ]]; then
      exists=1
      break
    fi
  done
  if [[ $exists -eq 0 ]]; then
    uniq_selected+=("$s")
  fi
done

for svc in "${uniq_selected[@]-}"; do
  start_one "$svc"
done

echo "Done."
