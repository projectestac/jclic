#!/bin/bash

# Builds flatpack package into 'build-dir'
flatpak-builder --force-clean build-dir edu.xtec.JClic.json

# Export build to a repository
# flatpak build-export my-repo build-dir

# Create a single file bundle from the repository
# flatpak build-bundle my-repo edu.xtec.JClic.flatpak edu.xtec.JClic
