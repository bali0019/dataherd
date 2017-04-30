package org.jas.plugins.utils.service.impl;

import org.jas.plugins.utils.entity.ReleaseScriptsInfo;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.jas.plugins.utils.repository.ReleaseScriptsRepository;
import org.jas.plugins.utils.service.IReleaseScriptsService;

/**
 * Created by jabali on 3/1/17.
 */
public class ReleaseScriptsServiceImpl implements IReleaseScriptsService {

    private ReleaseScriptsRepository releaseScriptsRepository;

    private MongoTemplate mongoTemplate;

    public ReleaseScriptsServiceImpl(ReleaseScriptsRepository releaseScriptsRepository,
                                     MongoTemplate mongoTemplate) {
        this.releaseScriptsRepository = releaseScriptsRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ReleaseScriptsInfo getLastReleaseTagScriptsRan() {

      return mongoTemplate.findOne(
                new Query().limit(1).with(new Sort(Sort.Direction.DESC,"releaseTag")),
                ReleaseScriptsInfo.class);
    }

    @Override
    public void save(ReleaseScriptsInfo releaseScriptsInfo) {

        releaseScriptsRepository.save(releaseScriptsInfo);

    }
}
