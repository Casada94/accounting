package com.asadaclan.accounting.repositories;

import java.util.UUID;

import com.asadaclan.accounting.entities.Undo;
import org.springframework.data.repository.CrudRepository;

public interface UndoRepository extends CrudRepository<Undo, UUID> {
}
