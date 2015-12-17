package fr.ifremer.sensornanny.sync.dao;

import fr.ifremer.sensornanny.sync.dto.model.Term;

public interface ITermDao {

    /**
     * Get a theme from it's identifier
     * 
     * @param id identifier for the term
     * @return term identified by id
     */
    Term getTerm(String id);

}
