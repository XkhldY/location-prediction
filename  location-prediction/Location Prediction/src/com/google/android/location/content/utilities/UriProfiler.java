package com.google.android.location.content.utilities;

import java.util.List;

import android.net.Uri;

/**
 * Utilities for dealing with content and other types of URIs.
 *
 * @author Andrei
 */
public class UriProfiler {

  public static boolean matchesContentUri(Uri uri, Uri baseContentUri) {
    if (uri == null) {
      return false;
    }

    // Check that scheme and authority are the same.
    if (!uri.getScheme().equals(baseContentUri.getScheme()) ||
        !uri.getAuthority().equals(baseContentUri.getAuthority())) {
      return false;
    }

    // Checks that all the base path components are in the URI.
    List<String> uriPathSegments = uri.getPathSegments();
    List<String> basePathSegments = baseContentUri.getPathSegments();
    if (basePathSegments.size() > uriPathSegments.size()) {
      return false;
    }
    for (int i = 0; i < basePathSegments.size(); i++) {
      if (!uriPathSegments.get(i).equals(basePathSegments.get(i))) {
        return false;
      }
    }

    return true;
  }

  public static boolean isFileUri(Uri uri) {
    return "file".equals(uri.getScheme());
  }

  private UriProfiler() {}
}