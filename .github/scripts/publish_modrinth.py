# SPDX-License-Identifier: GPL-3.0-only
# Serverify, copyright (C) 2026 Louchatfroff (https://github.com/Louchatfroff)

import json
import os
import re
import sys
import urllib.error
import urllib.request
import uuid
import zipfile
from pathlib import Path

API = "https://api.modrinth.com/v2"
USER_AGENT = "Louchatfroff/serverify (https://github.com/Louchatfroff/serverify)"
DEPENDENCIES = [
    {"project_id": "P7dR8mSH", "dependency_type": "required"},
    {"project_id": "Ha28R6CL", "dependency_type": "required"},
    {"project_id": "mOgUt4GM", "dependency_type": "optional"},
]
JAR_NAME = re.compile(r"^serverify-mc(\d+(?:\.\d+)*)-(.+)\.jar$")


def call(method, path, token=None, data=None, content_type=None):
    request = urllib.request.Request(API + path, data=data, method=method)
    request.add_header("User-Agent", USER_AGENT)
    if token:
        request.add_header("Authorization", token)
    if content_type:
        request.add_header("Content-Type", content_type)
    try:
        with urllib.request.urlopen(request) as response:
            return json.load(response)
    except urllib.error.HTTPError as error:
        detail = error.read().decode("utf8", "replace")[:500]
        sys.exit(f"Modrinth {method} {path} failed with HTTP {error.code}: {detail}")


def parse(version):
    return tuple(int(part) for part in version.split("."))


def in_range(version, expression):
    value = parse(version)
    for clause in expression.split():
        match = re.match(r"^(>=|<=|<|>|=)?(\d+(?:\.\d+)*)$", clause)
        operator, bound = match.group(1) or "=", parse(match.group(2))
        width = max(len(value), len(bound))
        v, b = value + (0,) * (width - len(value)), bound + (0,) * (width - len(bound))
        if not {"=": v == b, ">=": v >= b, "<=": v <= b, "<": v < b, ">": v > b}[operator]:
            return False
    return True


def supported_versions(jar, releases):
    with zipfile.ZipFile(jar) as archive:
        expression = json.loads(archive.read("fabric.mod.json"))["depends"]["minecraft"]
    return [v for v in releases if re.fullmatch(r"\d+(?:\.\d+)*", v) and in_range(v, expression)]


def multipart(data, jar):
    boundary = uuid.uuid4().hex
    parts = [
        f'--{boundary}\r\nContent-Disposition: form-data; name="data"\r\n'
        "Content-Type: application/json\r\n\r\n",
        data,
        f'\r\n--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{jar.name}"\r\n'
        "Content-Type: application/java-archive\r\n\r\n",
    ]
    body = b"".join(p.encode("utf8") for p in parts[:2])
    body += parts[2].encode("utf8") + jar.read_bytes()
    body += f"\r\n--{boundary}--\r\n".encode("utf8")
    return body, f"multipart/form-data; boundary={boundary}"


def main():
    jar_dir = Path(sys.argv[1])
    token = os.environ["MODRINTH_TOKEN"]
    project = os.environ["MODRINTH_PROJECT"]
    tag = os.environ["TAG"]
    changelog = os.environ.get("CHANGELOG", "")
    dry_run = os.environ.get("DRY_RUN") == "1"

    project_id = project if dry_run else call("GET", f"/project/{project}", token)["id"]
    existing = set() if dry_run else {
        v["version_number"] for v in call("GET", f"/project/{project_id}/version", token)
    }
    releases = [
        v["version"] for v in call("GET", "/tag/game_version") if v["version_type"] == "release"
    ]

    failures = 0
    for jar in sorted(jar_dir.glob("*.jar")):
        match = JAR_NAME.match(jar.name)
        if not match:
            continue
        minecraft, mod_version = match.groups()
        game_versions = supported_versions(jar, releases)
        version_number = f"{mod_version}+mc{minecraft}"
        if not game_versions:
            print(f"skipping {jar.name}: Modrinth does not list Minecraft {minecraft} yet")
            failures += 1
            continue
        if version_number in existing:
            print(f"skipping {jar.name}: {version_number} is already on Modrinth")
            continue
        data = json.dumps({
            "name": f"Serverify {mod_version} for Minecraft {minecraft}",
            "version_number": version_number,
            "changelog": changelog,
            "dependencies": DEPENDENCIES,
            "game_versions": game_versions,
            "version_type": "release",
            "loaders": ["fabric"],
            "featured": True,
            "project_id": project_id,
            "file_parts": ["file"],
            "primary_file": "file",
        })
        if dry_run:
            print(data)
            continue
        body, content_type = multipart(data, jar)
        result = call("POST", "/version", token, body, content_type)
        print(f"published {result['version_number']} for {', '.join(game_versions)}")
    return 1 if failures else 0


sys.exit(main())
