// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

import org.scalatest.Suites
import service.{FigureServiceTest, BannerServiceTest, BannerMobileServiceTest, AccountStoreTest, ConferenceServiceTest, AbstractServiceTest}
import util.serializer.SerializerTest
import controller.{AccountsCtrlTest, FigureCtrlTest, BannerCtrlTest, AbstractsCtrlTest, ConferenceCtrlTest}
import models.ConferenceTest


/**
 * Runs all unit tests
 */
class TestAll extends Suites(

  new SerializerTest,
  new AbstractServiceTest,
  new ConferenceServiceTest,
  new FigureServiceTest,
  new BannerServiceTest,
  new BannerMobileServiceTest,
  new AccountStoreTest,
  new ConferenceCtrlTest,
  new AbstractsCtrlTest,
  new FigureCtrlTest,
  new BannerCtrlTest,
  new AccountsCtrlTest,
  new ConferenceTest

)
