/*
Copyright 2022 The reform-org/reform contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package webapp.pages

import webapp.Repositories
import webapp.entity.*
import webapp.services.Toaster
import rescala.default.*
import webapp.components.common.*

import ContractSchemasPage.*
import webapp.services.RoutingService

case class ContractSchemasPage()(using repositories: Repositories, toaster: Toaster, routing: RoutingService)
    extends EntityPage[ContractSchema](
      "Contract schemas",
      repositories.contractSchemas,
      Seq(name, files),
      DefaultEntityRow(),
    ) {}

object ContractSchemasPage {
  private val name = UIAttributeBuilder.string
    .withLabel("Name")
    .require
    .bindAsText[ContractSchema](
      _.name,
      (s, a) => s.copy(name = a),
    )

  private def files(using repositories: Repositories): UIAttribute[ContractSchema, Seq[String]] =
    UIAttributeBuilder
      .multiSelect(
        repositories.requiredDocuments.all.map(list =>
          list.map(value => value.id -> value.signal.map(_.name.get.getOrElse(""))),
        ),
      )
      .withLabel("Required Documents")
      .require
      .bindAsMultiSelect[ContractSchema](
        _.files,
        (c, a) => c.copy(files = a),
      )
}
