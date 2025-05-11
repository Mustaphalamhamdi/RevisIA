package com.mustapha.revisia.dao;

import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import java.util.List;

public interface DocumentDAO {
    void saveDocument(Document document);
    Document getDocumentById(Long id);
    List<Document> getDocumentsByUser(User user);
    List<Document> getDocumentsBySubject(Subject subject);
    void updateDocument(Document document);
    void deleteDocument(Document document);
}