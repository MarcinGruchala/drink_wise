#!/usr/bin/env bash
set -euo pipefail

FLOW_TARGET="${1:-maestro/flows/home-smoke.yaml}"
if [[ $# -gt 0 ]]; then
  shift
fi
if [[ "${1:-}" == "--" ]]; then
  shift
fi
EXTRA_MAESTRO_ARGS=()
if [[ $# -gt 0 ]]; then
  EXTRA_MAESTRO_ARGS=("$@")
fi

APP_ID="${APP_ID:-com.mgruchala.drinkwise.dev}"
AVD_NAME="${AVD_NAME:-Pixel_10_Pro}"
ARTIFACT_ROOT="${ARTIFACT_ROOT:-artifacts/maestro}"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
OUTPUT_DIR="${ARTIFACT_ROOT}/${TIMESTAMP}"
SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}"

if [[ -n "${SDK_ROOT}" ]]; then
  export ANDROID_SDK_ROOT="${SDK_ROOT}"
  export ANDROID_HOME="${SDK_ROOT}"
fi

if ! command -v maestro >/dev/null 2>&1; then
  echo "maestro is not installed or is not on PATH." >&2
  exit 1
fi

ADB_BIN="${ADB_BIN:-$(command -v adb || true)}"
if [[ -z "${ADB_BIN}" && -n "${ANDROID_HOME:-}" && -x "${ANDROID_HOME}/platform-tools/adb" ]]; then
  ADB_BIN="${ANDROID_HOME}/platform-tools/adb"
fi
if [[ -z "${ADB_BIN}" ]]; then
  echo "adb is not installed or is not on PATH." >&2
  exit 1
fi

EMULATOR_BIN="${EMULATOR_BIN:-$(command -v emulator || true)}"
if [[ -z "${EMULATOR_BIN}" && -n "${SDK_ROOT}" && -x "${SDK_ROOT}/emulator/emulator" ]]; then
  EMULATOR_BIN="${SDK_ROOT}/emulator/emulator"
fi

first_device() {
  "${ADB_BIN}" devices | awk 'NR > 1 && $2 == "device" { print $1; exit }'
}

verify_avd_image() {
  local avd_config="${HOME}/.android/avd/${AVD_NAME}.avd/config.ini"
  local image_sysdir

  if [[ ! -f "${avd_config}" || -z "${SDK_ROOT}" ]]; then
    return
  fi

  image_sysdir="$(awk -F= '$1 == "image.sysdir.1" { print $2; exit }' "${avd_config}")"
  if [[ -n "${image_sysdir}" && ! -d "${SDK_ROOT}/${image_sysdir}" ]]; then
    echo "AVD '${AVD_NAME}' points to a missing system image:" >&2
    echo "  ${SDK_ROOT}/${image_sysdir}" >&2
    echo "Install or repair that system image in Android Studio before running Maestro." >&2
    exit 1
  fi
}

DEVICE_ID="${DEVICE_ID:-$(first_device)}"

if [[ -z "${DEVICE_ID}" ]]; then
  if [[ -z "${EMULATOR_BIN}" ]]; then
    echo "No connected Android device and emulator binary was not found." >&2
    exit 1
  fi

  verify_avd_image

  echo "No connected device found. Starting AVD '${AVD_NAME}'..."
  "${EMULATOR_BIN}" -avd "${AVD_NAME}" -no-snapshot-load >/tmp/drink-wise-emulator.log 2>&1 &
  EMULATOR_PID=$!

  for _ in {1..60}; do
    DEVICE_ID="$(first_device)"
    if [[ -n "${DEVICE_ID}" ]]; then
      break
    fi
    if ! kill -0 "${EMULATOR_PID}" >/dev/null 2>&1; then
      echo "Emulator process exited before a device became available." >&2
      echo "See /tmp/drink-wise-emulator.log." >&2
      exit 1
    fi
    sleep 2
  done

  if [[ -z "${DEVICE_ID}" ]]; then
    echo "Timed out waiting for emulator device. See /tmp/drink-wise-emulator.log." >&2
    exit 1
  fi

  BOOT_COMPLETED=false
  for _ in {1..120}; do
    if [[ "$("${ADB_BIN}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
      BOOT_COMPLETED=true
      break
    fi
    sleep 2
  done

  if [[ "${BOOT_COMPLETED}" != "true" ]]; then
    echo "Emulator did not finish booting. See /tmp/drink-wise-emulator.log." >&2
    exit 1
  fi

  DEVICE_ID="$(first_device)"
  if [[ -z "${DEVICE_ID}" ]]; then
    echo "Emulator did not become available. See /tmp/drink-wise-emulator.log." >&2
    exit 1
  fi
fi

"${ADB_BIN}" -s "${DEVICE_ID}" shell input keyevent 82 >/dev/null 2>&1 || true

echo "Building debug APK..."
./gradlew assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -f "${APK}" ]]; then
  echo "Debug APK not found at ${APK}." >&2
  exit 1
fi

echo "Installing ${APK} on ${DEVICE_ID}..."
"${ADB_BIN}" -s "${DEVICE_ID}" install -r "${APK}"

mkdir -p "${OUTPUT_DIR}"

echo "Running Maestro target ${FLOW_TARGET}..."
MAESTRO_CMD=(maestro test --device "${DEVICE_ID}" --test-output-dir "${OUTPUT_DIR}")
if [[ ${#EXTRA_MAESTRO_ARGS[@]} -gt 0 ]]; then
  MAESTRO_CMD+=("${EXTRA_MAESTRO_ARGS[@]}")
fi
MAESTRO_CMD+=(-e "APP_ID=${APP_ID}" "${FLOW_TARGET}")
"${MAESTRO_CMD[@]}"

echo "Maestro artifacts: ${OUTPUT_DIR}"
