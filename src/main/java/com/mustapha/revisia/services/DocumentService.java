// DocumentService.java
package com.mustapha.revisia.services;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import java.util.List;

public interface DocumentService {
    void saveDocument(Document document);
    void createDocument(String title, String fileName, String filePath, String description, Subject subject, User user);
    Document getDocumentById(Long id);
    List<Document> getDocumentsByUser(User user);
    List<Document> getDocumentsBySubject(Subject subject);
    void updateDocument(Document document);
    void deleteDocument(Document document);
}