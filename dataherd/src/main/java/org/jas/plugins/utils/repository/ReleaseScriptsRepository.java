package org.jas.plugins.utils.repository;

import org.jas.plugins.utils.entity.ReleaseScriptsInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by jabali on 3/1/17.
 */


@Repository
public interface ReleaseScriptsRepository extends MongoRepository<ReleaseScriptsInfo, String> {

}
