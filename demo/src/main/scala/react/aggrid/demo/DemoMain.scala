package react.aggrid.demo

import scala.scalajs.js.annotation.JSExportTopLevel

import org.scalajs.dom
import scala.scalajs.js
import js.annotation._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import reactST.agGridReact.components._
import reactST.agGridCommunity.iCellRendererMod.ICellRendererParams
import reactST.agGridCommunity.iCellEditorMod.ICellEditorParams

@JSExportTopLevel("DemoMain")
object DemoMain {

  def renderFn(props: ICellRendererParams) = renderer(
    props.value.asInstanceOf[String]
  ).raw

  def editFn(props: ICellEditorParams) = Editor.component(props.value.asInstanceOf[String]).raw

  val renderer = ScalaComponent
    .builder[String]
    .render_P((props: String) => <.span(s"I need a: ${props}"))
    // .render_P((props: String) => <.span(props))
    .build

  val jsRenderer =
    renderer
      .cmapCtorProps[ICellRendererParams](p => p.value.asInstanceOf[String] + "?")
      .toJsComponent
      .raw

  dom.console.log(Editor.jsComponent.asInstanceOf[js.Any])
  dom.console.log(Editor.component("XXX").raw.asInstanceOf[js.Any])

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
          AgGridReact
            .disableStaticMarkup(
              true
            ) // necessary to keep cell renderers from duplicating in the first cell
            .rowData(rowData)(
              AgGridColumn.ColDef
                .field("make"),
              AgGridColumn.ColDef
                .field("model")
                .editable(true)
                // .cellRendererFramework(renderFn _)
                .cellRendererFramework(jsRenderer)
                // .cellEditorFramework(editFn _),
                .cellEditorFramework(Editor.jsComponent),
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

@JSExportTopLevel("Editor")
object Editor {

  // @JSExportAll
  class Backend() {
    def render(p: String): VdomNode =
      <.input(^.defaultValue := p, ^.width := "100%")

    // val getValue: js.Function0[js.Any] = () => "Edited, eh?"

    // @JSExport
    def getValue(): js.Any = "Edited"

    def isCancelBeforeStart() = { println("isCancelBeforeStart() called"); false }
    def isCancelAfterEnd() = { println("isCancelAfterEnd called"); false }
    def afterGuiAttached() = println("Gui Attached")
  }

  def fromProps(q: ICellEditorParams): String = q.value.asInstanceOf[String] + "!"

  val component = ScalaComponent.builder[String].renderBackend[Backend].build

  // @JSExport
  val jsComponent = component.cmapCtorProps[ICellEditorParams](fromProps).toJsComponent.raw
}
