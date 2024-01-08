import json
import os
import xml.etree.ElementTree as ET
from pathlib import Path

import httpx

BRANCH = os.getenv("BRANCH", "development")
PR_NUMBER = os.getenv("PR_NUMBER", "0")
REPO = os.getenv("REPO", "fga")
OWNER = os.getenv("OWNER", "ArthurKun21")

URL_BASE = f"https://api.github.com/repos/{OWNER}/{REPO}"

cwd = Path.cwd()


def save_releases_summary(summary: str):
    strings_xml_path = cwd / "app" / "src" / "main" / "res" / "values" / "strings.xml"
    tree = ET.parse(strings_xml_path)
    root = tree.getroot()

    element = root.find("./string[@name='release_notes_summary']")
    if element is not None:
        element.text = summary
        tree.write(strings_xml_path)


def get_commit_message(sha: str):
    commit_url = f"{URL_BASE}/git/commits/{sha}"
    response = httpx.get(commit_url)
    data = json.loads(response.text)
    message = data["message"]
    return message


def get_latest_commit():
    last_commit = f"{URL_BASE}/commits"
    response = httpx.get(last_commit)
    data = json.loads(response.text)
    last_commit_sha = data[0]["sha"]
    message = get_commit_message(last_commit_sha)
    return message


def url_tag_commit_sha(tag: str):
    URL_TAG = f"{URL_BASE}/git/refs/tags/{tag}"
    response = httpx.get(URL_TAG)
    data = json.loads(response.text)
    return data["object"]["sha"]


def get_last_releases():
    last_releases = f"{URL_BASE}/releases"
    response = httpx.get(last_releases)
    body = json.loads(response.text)

    commit_info_list = []

    for release in body:
        tag = release["tag_name"]

        tag_sha = url_tag_commit_sha(tag)

        tag_commit_message = get_commit_message(tag_sha)

        commit_info_list.append(f"{tag}\n{tag_commit_message}")
    return commit_info_list


def development_branch():
    latest_commit = get_latest_commit()
    last_releases_information = get_last_releases()
    information = [f"Latest\n{latest_commit}"] + last_releases_information

    information_string = "\n".join(information)

    save_releases_summary(information_string)


def pull_request_branch():
    URL_PR = f"{URL_BASE}/pulls/{PR_NUMBER}/commits"
    response = httpx.get(URL_PR)
    data = json.loads(response.text)

    commit_info_list = []

    for commit in data:
        sha = commit["sha"]

        message = get_commit_message(sha)
        commit_info_list.append(message)

    information_string = "\n".join(commit_info_list)

    save_releases_summary(information_string)


def main():
    if BRANCH == "development":
        development_branch()
    else:
        pull_request_branch()


if __name__ == "__main__":
    main()
