// Copyright © 2019, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import models.Model._
import javax.persistence.{Column, Entity, ManyToOne}

/**
  * A model for banners.
  */
@Entity
class Banner extends Model {

  @Column(length=300)
  var bType: String = _

  @ManyToOne
  var conference: Conference = _

}

object Banner {

  def apply(uuid: Option[String],
            bType: Option[String],
            conference: Option[Conference] = None) : Banner = {

    val banner = new Banner()

    banner.uuid     = unwrapRef(uuid)
    banner.bType     = unwrapRef(bType)
    banner.conference    = unwrapRef(conference)

    banner
  }

}
