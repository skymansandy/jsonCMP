#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MODULE="json-cmp"

# Colors
BOLD='\033[1m'
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
RESET='\033[0m'

usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Publish JsonCMP artifacts to local or remote Maven repositories.

When run without flags, an interactive menu guides you through the options.
Flags skip the corresponding interactive prompts.

OPTIONS:
    --local              Publish to mavenLocal (~/.m2/repository)
    --remote             Publish to remote Maven repository (requires credentials)
    --version <version>  Override the version (default: from gradle.properties)
    --dry-run            Print what would be executed without running
    -h, --help           Show this help message

EXAMPLES:
    # Interactive mode
    $(basename "$0")

    # Non-interactive: publish to mavenLocal
    $(basename "$0") --local

    # Non-interactive: publish to remote with version override
    $(basename "$0") --remote --version 1.0.0-beta1

ENVIRONMENT VARIABLES (for --remote):
    MAVEN_REPO_URL         Remote Maven repository URL
    MAVEN_REPO_USERNAME    Repository username
    MAVEN_REPO_PASSWORD    Repository password/token

    Alternatively, set gpr.user and gpr.key in ~/.gradle/gradle.properties
EOF
    exit 0
}

# Defaults
TARGET=""
TARGET_FROM_FLAG=false
DRY_RUN=false
VERSION_OVERRIDE=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case "$1" in
        --local)    TARGET="local";  TARGET_FROM_FLAG=true; shift ;;
        --remote)   TARGET="remote"; TARGET_FROM_FLAG=true; shift ;;
        --version)  VERSION_OVERRIDE="$2";     shift 2 ;;
        --dry-run)  DRY_RUN=true;              shift ;;
        -h|--help)  usage ;;
        *)
            echo -e "${RED}Error: Unknown option '$1'${RESET}"
            echo ""
            usage
            ;;
    esac
done

# ── Interactive prompts (only for values not already set via flags) ───────────

prompt_target() {
    if [[ -n "$TARGET" ]]; then
        return
    fi
    echo ""
    echo -e "${BOLD}Where do you want to publish?${RESET}"
    echo ""
    echo "  1) Local   (~/.m2/repository)"
    echo "  2) Remote  (Maven repository)"
    echo ""
    while true; do
        read -r -p "Select [1/2]: " choice
        case "$choice" in
            1) TARGET="local";  break ;;
            2) TARGET="remote"; break ;;
            *) echo -e "${YELLOW}Please enter 1 or 2.${RESET}" ;;
        esac
    done
}

prompt_version() {
    if [[ -n "$VERSION_OVERRIDE" ]] || [[ "$TARGET_FROM_FLAG" == true ]]; then
        return
    fi

    local default_version
    default_version=$(grep 'jsoncmp.version=' "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)

    echo ""
    echo -e "${BOLD}Version to publish:${RESET} ${CYAN}${default_version}${RESET}"
    read -r -p "Press Enter to keep, or type a new version: " input
    if [[ -n "$input" ]]; then
        VERSION_OVERRIDE="$input"
    fi
}

run_cmd() {
    if [[ "$DRY_RUN" == true ]]; then
        echo -e "  ${YELLOW}[dry-run]${RESET} $*"
    else
        echo -e "  ${CYAN}>>>${RESET} $*"
        "$@"
    fi
}

# ── Run interactive prompts ──────────────────────────────────────────────────

prompt_target
prompt_version

# ── Build configuration ─────────────────────────────────────────────────────

VERSION_FLAG=""
if [[ -n "$VERSION_OVERRIDE" ]]; then
    VERSION_FLAG="-Pjsoncmp.version=$VERSION_OVERRIDE"
fi

if [[ "$TARGET" == "local" ]]; then
    MAVEN_TASK="publishToMavenLocal"
else
    MAVEN_TASK="publishAllPublicationsToGitHubPackagesRepository"
fi

cd "$PROJECT_ROOT"

# ── Summary ──────────────────────────────────────────────────────────────────

echo ""
echo -e "${BOLD}==========================================${RESET}"
echo -e "${BOLD} JsonCMP Publisher${RESET}"
echo -e "${BOLD}==========================================${RESET}"
echo -e "  Target:  ${GREEN}${TARGET}${RESET}"
if [[ -n "$VERSION_OVERRIDE" ]]; then
    echo -e "  Version: ${GREEN}${VERSION_OVERRIDE}${RESET}"
else
    echo -e "  Version: ${GREEN}$(grep 'jsoncmp.version=' "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2)${RESET}"
fi
echo -e "  Module:  ${GREEN}${MODULE}${RESET}"
if [[ "$DRY_RUN" == true ]]; then
    echo -e "  Mode:    ${YELLOW}dry-run${RESET}"
fi
echo -e "${BOLD}==========================================${RESET}"
echo ""

# ── Publish ──────────────────────────────────────────────────────────────────

echo -e "${BOLD}--- ${MODULE} ---${RESET}"
run_cmd ./gradlew ":${MODULE}:${MAVEN_TASK}" $VERSION_FLAG
echo ""

echo -e "${GREEN}${BOLD}Done!${RESET}"
