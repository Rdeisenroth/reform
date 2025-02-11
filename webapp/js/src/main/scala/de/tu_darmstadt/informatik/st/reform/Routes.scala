/*
Copyright 2022 https://github.com/phisn/ratable, The reform-org/reform contributors

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
package de.tu_darmstadt.informatik.st.reform

import colibri.*
import colibri.router.*
import de.tu_darmstadt.informatik.st.reform.pages.DocumentsPage
import de.tu_darmstadt.informatik.st.reform.pages.*
import de.tu_darmstadt.informatik.st.reform.services.Page
import org.scalajs.dom.window
object Routes {
  def fromPath(using
      jsImplicits: JSImplicits,
  ): Path => Page = {
    case Root                           => HomePage()
    case Root / "projects"              => ProjectsPage()
    case Root / "users"                 => UsersPage()
    case Root / "hiwis"                 => HiwisPage()
    case Root / "payment-levels"        => PaymentLevelsPage()
    case Root / "salary-changes"        => SalaryChangesPage()
    case Root / "supervisors"           => SupervisorsPage()
    case Root / "contract-schemas"      => ContractSchemasPage()
    case Root / "contracts"             => ContractsPage()
    case Root / "contract-drafts"       => ContractDraftsPage()
    case Root / "edit-contracts" / id   => EditContractsPage(id)
    case Root / "new-contract"          => NewContractPage()
    case Root / "extend-contracts" / id => ExtendContractPage(id)
    case Root / "documents"             => DocumentsPage()
    case Root / "settings"              => SettingsPage()
    case _                              => ErrorPage()
  }

  def toPath: Page => Path = {
    case HomePage()             => Root / ""
    case ProjectsPage()         => Root / "projects"
    case UsersPage()            => Root / "users"
    case HiwisPage()            => Root / "hiwis"
    case PaymentLevelsPage()    => Root / "payment-levels"
    case SalaryChangesPage()    => Root / "salary-changes"
    case SupervisorsPage()      => Root / "supervisors"
    case ContractSchemasPage()  => Root / "contract-schemas"
    case ContractsPage()        => Root / "contracts"
    case ContractDraftsPage()   => Root / "contract-drafts"
    case EditContractsPage(id)  => Root / "edit-contracts" / id
    case ExtendContractPage(id) => Root / "extend-contracts" / id
    case NewContractPage()      => Root / "new-contract"
    case DocumentsPage()        => Root / "documents"
    case SettingsPage()         => Root / "settings"
    case ErrorPage()            => Root / window.location.pathname.nn.substring(1).nn
  }
}
