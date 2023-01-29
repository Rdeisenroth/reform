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
package webapp.entity

import kofre.base.*
import org.scalajs.dom
import org.scalajs.dom.window
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.navigationHeader
import webapp.repo.Repository
import webapp.repo.Synced
import webapp.services.Page
import webapp.services.RoutingService
import webapp.webrtc.WebRTCService
import webapp.{*, given}

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext.Implicits.global

private class EntityRow[T <: Entity[T]](
    repository: Repository[T],
    existingValue: Option[Synced[T]],
    editingValue: Var[Option[T]],
    uiAttributes: Seq[UICommonAttribute[T, ? <: Any]],
)(using bottom: Bottom[T], lattice: Lattice[T]) {

  def render() = {
    editingValue.map(editingNow => {
      val res = editingNow match {
        case Some(_) => {
          val res = Some(
            tr(
              cls := "border-b  dark:border-gray-700", // "hover:bg-violet-100 dark:hover:bg-violet-900 border-b hover:bg-gray-100 dark:hover:bg-gray-600 ",
              uiAttributes.map(ui => {
                ui.renderEdit(s"form-${existingValue.map(_.id)}", editingValue)
              }),
              td(
                cls := "py-1 space-x-1 w-1/6",
                form(
                  idAttr := s"form-${existingValue.map(_.id)}",
                  onSubmit.foreach(e => {
                    e.preventDefault()
                    createOrUpdate()
                  }),
                ), {
                  existingValue match {
                    case Some(p) => {
                      List(
                        button(
                          cls := "btn",
                          formId := s"form-${existingValue.map(_.id)}",
                          `type` := "submit",
                          idAttr := "add-entity-button",
                          "Save edit",
                        ),
                        button(
                          cls := "btn",
                          idAttr := "add-entity-button",
                          "Cancel",
                          onClick.foreach(_ => cancelEdit()),
                        ),
                      )
                    }
                    case None => {
                      button(
                        cls := "btn",
                        formId := s"form-${existingValue.map(_.id)}",
                        `type` := "submit",
                        idAttr := "add-entity-button",
                        "Add Entity",
                      )
                    }
                  }
                },
                existingValue.map(p => {
                  button(cls := "btn btn-error btn-square", "X", onClick.foreach(_ => removeEntity(p)))
                }),
              ),
            ),
          )
          Var(res)
        }
        case None => {
          val res: Signal[Option[VNode]] = existingValue match {
            case Some(syncedEntity) => {
              val res = syncedEntity.signal.map(p => {
                val res = if (p.exists.get.getOrElse(true)) {
                  Some(
                    tr(
                      cls := "border-b hover:bg-violet-100 dark:hover:bg-gray-600 dark:border-gray-700", // "border-b hover:bg-gray-100 dark:hover:bg-violet-900 dark:border-violet-900",
                      data.id := syncedEntity.id,
                      uiAttributes.map(ui => {
                        ui.render(p)
                      }),
                      td(
                        cls := "py-1 space-x-1 w-1/6",
                        button(
                          cls := "btn",
                          "Edit",
                          onClick.foreach(_ => startEditing()),
                        ),
                        button(cls := "btn btn-error btn-square", "X", onClick.foreach(_ => removeEntity(syncedEntity))),
                      ),
                    ),
                  )
                } else {
                  None
                }
                res
              })
              res
            }
            case None => Var(None)
          }
          res
        }
      }
      res
    })
  }

  def removeEntity(p: Synced[T]): Unit = {
    val yes = window.confirm(s"Do you really want to delete the entity \"${p.signal.now.identifier.get}\"?")
    if (yes) {
      p.update(p => p.withExists(false))
    }
  }

  def cancelEdit(): Unit = {
    editingValue.set(None)
  }

  def createOrUpdate(): Unit = {
    val editingNow = editingValue.now
    (existingValue match {
      case Some(existing) => {
        existing.update(p => {
          p.merge(editingNow.get)
        })
        editingValue.set(None)
      }
      case None => {
        repository
          .create()
          .map(entity => {
            //  TODO FIXME we probably should special case initialization and not use the event
            entity.update(p => {
              p.merge(editingNow.get)
            })
            editingValue.set(Some(bottom.empty.default))
          })
      }
    })
  }

  private def startEditing(): Unit = {
    editingValue.set(Some(existingValue.get.signal.now))
  }
}

abstract class EntityPage[T <: Entity[T]](repository: Repository[T], uiAttributes: Seq[UICommonAttribute[T, ? <: Any]])(
    using
    bottom: Bottom[T],
    lattice: Lattice[T],
) extends Page {

  private val newUserRow: EntityRow[T] =
    EntityRow[T](repository, None, Var(Some(bottom.empty.default)), uiAttributes)

  def render(using routing: RoutingService, repositories: Repositories, webrtc: WebRTCService): VNode = {
    div(
      navigationHeader,
      div(
        cls := "relative overflow-x-auto shadow-md sm:rounded-lg pt-4 ", // " px-4 py-4 items-center w-full  mx-auto my-5 bg-white rounded-lg shadow-md",
        table(
          cls := "w-full text-left table-auto border-collapse ", // border-separate border-spacing-y-4
          // cls := "table-auto",
          thead(
            tr(
              uiAttributes.map(a => th(cls := "px-6 py-0 border-b-2 dark:border-gray-500", a.label)),
              th(cls := "px-6 py-0 border-b-2 dark:border-gray-500", "Stuff"),
            ),
          ),
          tbody(
            renderEntities(repository.all),
            newUserRow.render(),
          ),
        ),
      ),
    )
  }

  private def renderEntities(
      entities: Signal[List[Synced[T]]],
  ) =
    entities.map(
      _.map(syncedEntity => {
        EntityRow[T](repository, Some(syncedEntity), Var(None), uiAttributes).render()
      }),
    )
}
