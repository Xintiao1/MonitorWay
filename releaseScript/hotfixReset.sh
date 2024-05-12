#!/bin/sh

VERSION=`cat $1/1.VersionManage/ReleaseVersion.txt`
ssh sede0899.monitorway.net "rm -rf /opt/devtool/download/version/$VERSION"