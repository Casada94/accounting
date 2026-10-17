package com.asadaclan.accounting.services;

import org.springframework.stereotype.Service;

import com.asadaclan.accounting.repositories.UndoRepository;

import com.asadaclan.accounting.entities.Undo;

@Service
public class UndoService {

    private final UndoRepository undoRepository;

    public UndoService(UndoRepository undoRepository) {
        this.undoRepository = undoRepository;
    }

    public Undo undo(Undo undo) {
        return undoRepository.save(undo);
    }
}
