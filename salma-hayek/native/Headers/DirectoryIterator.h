#ifndef DIRECTORY_ITERATOR_H_included
#define DIRECTORY_ITERATOR_H_included

#include "toString.h"
#include "unix_exception.h"

#include <dirent.h>
#include <errno.h>
#include <string>
#include <sys/types.h>

struct DirectoryEntry {
private:
  std::string name;
  
public:
  std::string getName() const {
    return name;
  }
  
  DirectoryEntry() {
  }
  explicit DirectoryEntry(const dirent* cStyleEntry)
  : name(cStyleEntry->d_name) {
  }
};

struct DirectoryIterator {
private:
  std::string m_directoryName;
  DIR* m_handle;
  bool m_eof;
  DirectoryEntry m_entry;
  
private:
  void readOneEntry() {
    errno = 0;
    const dirent* cStyleEntry = readdir(m_handle);
    if (cStyleEntry != 0) {
      m_entry = DirectoryEntry(cStyleEntry);
      return;
    }
    m_eof = true;
    if (errno != 0) {
      throw unix_exception(std::string("readdir(\"") + m_directoryName + "\" " + toString(m_handle) + ") failed");
    }
  }
  
  DirectoryIterator(DirectoryIterator&);
  void operator=(DirectoryIterator&);
  
public:
  explicit DirectoryIterator(const std::string& directoryName)
  : m_directoryName(directoryName)
  , m_handle(opendir(directoryName.c_str()))
  , m_eof(false)
  {
    if (m_handle == 0) {
      throw unix_exception(std::string("opendir(\"") + m_directoryName + "\") failed");
    }
    readOneEntry();
  }
  
  ~DirectoryIterator() {
    closedir(m_handle);
  }
  
  bool isValid() const {
    return m_eof == false;
  }
  
  const DirectoryEntry* operator->() const {
    return &m_entry;
  }
  
  DirectoryIterator& operator++() {
    readOneEntry();
    return *this;
  }
};

#endif
