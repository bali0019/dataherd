package org.jas.plugins.utils.service;

import org.jas.plugins.utils.entity.ReleaseScriptsInfo;

/**
 * Created by jabali on 3/1/17.
 */
public interface IReleaseScriptsService {



    ReleaseScriptsInfo getLastReleaseTagScriptsRan();


    void save(ReleaseScriptsInfo releaseScriptsInfo);



}
