// DocumentServiceImpl.java
package com.mustapha.revisia.services;

import com.mustapha.revisia.dao.DocumentDAO;
import com.mustapha.revisia.dao.DocumentDAOImpl;
import com.mustapha.revisia.models.Document;
import com.mustapha.revisia.models.Subject;
import com.mustapha.revisia.models.User;
import java.util.List;

public class DocumentServiceImpl implements DocumentService {

    private final DocumentDAO documentDAO = new DocumentDAOImpl();

    @Override
    public void saveDocument(Document document) {
        documentDAO.saveDocument(document);
    }

    @Override
    public void createDocument(String title, String fileName, String filePath, String description, Subject subject, User user) {
        Document document = new Document(title, fileName, filePath, description, subject, user);
        saveDocument(document);
    }

    @Override
    public Document getDocumentById(Long id) {
        return documentDAO.getDocumentById(id);
    }

    @Override
    public List<Document> getDocumentsByUser(User user) {
        return documentDAO.getDocumentsByUser(user);
    }

    @Override
    public List<Document> getDocumentsBySubject(Subject subject) {
        return documentDAO.getDocumentsBySubject(subject);
    }

    @Override
    public void updateDocument(Document document) {
        documentDAO.updateDocument(document);
    }

    @Override
    public void deleteDocument(Document document) {
        documentDAO.deleteDocument(document);
    }
}