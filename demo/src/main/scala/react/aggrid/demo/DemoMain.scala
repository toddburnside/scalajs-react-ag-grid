package react.aggrid.demo

import scala.scalajs.js.annotation.JSExportTopLevel

import org.scalajs.dom
import scala.scalajs.js
import js.annotation._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import lucuma.agGridReact.components._

@JSExportTopLevel("DemoMain")
object DemoMain {

  trait RowData extends js.Object {
    val make: String
    val model: String
    val year: Int
  }

  object RowData {
    def apply(make: String, model: String, year: Int): RowData =
      js.Dynamic.literal("make" -> make, "model" -> model, "year" -> year).asInstanceOf[RowData]
  }

  val component =
    ScalaComponent
      .builder[Unit]
      .render { _ =>
        val rowData =
          js.Array(RowData("Fender", "Stratocaster", 2019),
                   RowData("Gibson", "Les Paul", 1958),
                   RowData("Fender", "Telecaster", 1971)
          )

        <.div(
          ^.cls := "ag-theme-alpine",
          ^.height := "400px",
          ^.width := "600px",
          AgGridReact.rowData(rowData)(AgGridColumn.ColDef.field("make"),
                                       AgGridColumn.ColDef.field("model"),
                                       AgGridColumn.ColDef.field("year")
          )
        )
      }
      .build

  @JSImport("ag-grid-community/dist/styles/ag-grid.css", JSImport.Default)
  @js.native
  object GridCss extends js.Object

  @JSImport("ag-grid-community/dist/styles/ag-theme-alpine.css", JSImport.Default)
  @js.native
  object GridThemeCss extends js.Object

  val gridCss      = GridCss
  val gridThemeCss = GridThemeCss

  @JSExport
  def main(): Unit = {

    val container = Option(dom.document.getElementById("root")).getOrElse {
      val elem = dom.document.createElement("div")
      elem.id = "root"
      dom.document.body.appendChild(elem)
      elem
    }

    component().renderIntoDOM(container)

    ()
  }
}
