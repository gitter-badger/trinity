package org.sisioh.trinity.domain.controller

import java.util.UUID
import org.sisioh.dddbase.core.lifecycle.sync.SyncRepository
import org.sisioh.dddbase.core.model.Identity

trait ControllerRepository
  extends SyncRepository[Identity[UUID], Controller]


