/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.xtec.jclic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 *
 * @author fbusquet
 */
public class HTML5Lib {

  protected File basePath;

  public String baseURL = "./";
  public String projectsBaseURL = "./projects/";
  public HashMap<String, String> languages = new HashMap<>();
  public String defaultLanguage = "en";
  public HashMap<String, String> title = new HashMap<>();
  public HashMap<String, String> description = new HashMap<>();
  public String logo = "logo.png";
  public HashMap<String, HashMap<String, String>> labels = new HashMap<>();
  public String indexPath = "projects";
  public String indexFile = "projects.json";

  public String[] actLanguagesOptions = {"*"};
  public HashMap<String, String[]> actLanguagesValues = new HashMap<>();
  public String[] actSubjectsOptions = {"*"};
  public HashMap<String, String[]> actSubjectsValues = new HashMap<>();
  public String[] actLevelsOptions = {"*"};
  public HashMap<String, String[]> actLevelsValues = new HashMap<>();
  public String[] actTagsOptions = {"*"};
  public HashMap<String, String[]> actTagsValues = new HashMap<>();

  public HTML5Lib(File basePath) {
    languages.put("en", "English");
    title.put("en", "JClic projects library");
    description.put("en", "Custom library of JClic projects");
    labels.put("en", new HashMap<String, String>());

    actLanguagesValues.put("en", new String[]{"All languages"});
    actSubjectsValues.put("en", new String[]{"All subjects"});
    actLevelsValues.put("en", new String[]{"All levels"});
    actTagsValues.put("en", new String[]{"All tags"});

    this.basePath = basePath;
  }

  public String getDefaultStr(HashMap<String, String> map) {
    return map.get(defaultLanguage);
  }

  public String[] getDefaultArray(HashMap<String, String[]> map) {
    return map.get(defaultLanguage);
  }

  public static HTML5Lib getLib(File mainJson) throws Exception {

    HTML5Lib result = new HTML5Lib(mainJson.getParentFile());
    JsonParser parser = new JsonParser();
    JsonElement element = parser.parse(new FileReader(mainJson));
    JsonObject obj = element.getAsJsonObject();
    JsonObject obj2;
    JsonArray array;
    int arraySize;
    Iterator<JsonElement> it;
    Iterator<Entry<String, JsonElement>> ite;
    JsonElement elem;
    Entry<String, JsonElement> entry;
    String key, value;

    if (obj.get("baseURL") == null) {
      throw new Exception("This is not a JClic HTML5 library!");
    }

    result.baseURL = obj.get("baseURL").getAsString();
    result.projectsBaseURL = obj.get("projectsBaseURL").getAsString();
    result.defaultLanguage = obj.get("defaultLanguage").getAsString();
    result.logo = obj.get("logo").getAsString();

    it = obj.get("languages").getAsJsonArray().iterator();
    while (it.hasNext()) {
      obj2 = it.next().getAsJsonObject();
      result.languages.put(obj2.get("id").getAsString(), obj2.get("name").getAsString());
    }

    ite = obj.get("title").getAsJsonObject().entrySet().iterator();
    while (ite.hasNext()) {
      entry = ite.next();
      result.title.put(entry.getKey(), entry.getValue().getAsString());
    }

    ite = obj.get("description").getAsJsonObject().entrySet().iterator();
    while (ite.hasNext()) {
      entry = ite.next();
      result.description.put(entry.getKey(), entry.getValue().getAsString());
    }

    result.indexPath = obj.get("index").getAsJsonObject().get("path").getAsString();
    result.indexFile = obj.get("index").getAsJsonObject().get("file").getAsString();

    // Read languages
    array = obj.get("actLanguages").getAsJsonObject().get("options").getAsJsonArray();
    int nLanguages = array.size();
    result.actLanguagesOptions = new String[nLanguages];
    for (int i = 0; i < nLanguages; i++) {
      result.actLanguagesOptions[i] = array.get(i).getAsString();
    }
    ite = obj.get("actLanguages").getAsJsonObject().entrySet().iterator();
    while (ite.hasNext()) {
      entry = ite.next();
      key = entry.getKey();
      if (!"options".equals(key)) {
        String[] langs = new String[nLanguages];
        HashMap<String, String> map = new HashMap<>();
        array = entry.getValue().getAsJsonArray();
        arraySize = array.size();
        for (int i = 0; i < arraySize; i++) {
          obj2 = array.get(i).getAsJsonObject();
          map.put(obj2.get("val").getAsString(), obj2.get("text").getAsString());
        }
        for (int i = 0; i < nLanguages; i++) {
          langs[i] = map.get(result.actLanguagesOptions[i]);
        }
        result.actLanguagesValues.put(key, langs);
      }
    }

    // Read levels
    array = obj.get("actLevels").getAsJsonObject().get("options").getAsJsonArray();
    int nLevels = array.size();
    result.actLevelsOptions = new String[nLevels];
    for (int i = 0; i < nLevels; i++) {
      result.actLevelsOptions[i] = array.get(i).getAsString();
    }
    ite = obj.get("actLevels").getAsJsonObject().entrySet().iterator();
    while (ite.hasNext()) {
      entry = ite.next();
      key = entry.getKey();
      if (!"options".equals(key)) {
        String[] levels = new String[nLevels];
        HashMap<String, String> map = new HashMap<>();
        array = entry.getValue().getAsJsonArray();
        arraySize = array.size();
        for (int i = 0; i < arraySize; i++) {
          obj2 = array.get(i).getAsJsonObject();
          map.put(obj2.get("val").getAsString(), obj2.get("text").getAsString());
        }
        for (int i = 0; i < nLevels; i++) {
          levels[i] = map.get(result.actLevelsOptions[i]);
        }
        result.actLevelsValues.put(key, levels);
      }
    }

    // Read subjects
    array = obj.get("actSubjects").getAsJsonObject().get("options").getAsJsonArray();
    int nSubjects = array.size();
    result.actSubjectsOptions = new String[nSubjects];
    for (int i = 0; i < nSubjects; i++) {
      result.actSubjectsOptions[i] = array.get(i).getAsString();
    }
    ite = obj.get("actSubjects").getAsJsonObject().entrySet().iterator();
    while (ite.hasNext()) {
      entry = ite.next();
      key = entry.getKey();
      if (!"options".equals(key)) {
        String[] subjects = new String[nSubjects];
        HashMap<String, String> map = new HashMap<>();
        array = entry.getValue().getAsJsonArray();
        arraySize = array.size();
        for (int i = 0; i < arraySize; i++) {
          obj2 = array.get(i).getAsJsonObject();
          map.put(obj2.get("val").getAsString(), obj2.get("text").getAsString());
        }
        for (int i = 0; i < nSubjects; i++) {
          subjects[i] = map.get(result.actSubjectsOptions[i]);
        }
        result.actSubjectsValues.put(key, subjects);
      }
    }

    // Read tags
    if (obj.get("actTags") != null) {
      array = obj.get("actTags").getAsJsonObject().get("options").getAsJsonArray();
      int nTags = array.size();
      result.actTagsOptions = new String[nTags];
      for (int i = 0; i < nTags; i++) {
        result.actTagsOptions[i] = array.get(i).getAsString();
      }
      ite = obj.get("actTags").getAsJsonObject().entrySet().iterator();
      while (ite.hasNext()) {
        entry = ite.next();
        key = entry.getKey();
        if (!"options".equals(key)) {
          String[] tags = new String[nTags];
          HashMap<String, String> map = new HashMap<>();
          array = entry.getValue().getAsJsonArray();
          arraySize = array.size();
          for (int i = 0; i < arraySize; i++) {
            obj2 = array.get(i).getAsJsonObject();
            map.put(obj2.get("val").getAsString(), obj2.get("text").getAsString());
          }
          for (int i = 0; i < nTags; i++) {
            tags[i] = map.get(result.actTagsOptions[i]);
          }
          result.actTagsValues.put(key, tags);
        }
      }
    }
    
    return result;
  }

}
