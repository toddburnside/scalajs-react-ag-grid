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
import scala.annotation.unused

@JSExportTopLevel("DemoMain")
object DemoMain {
  import Editor.logit

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

  logit("Raw:", Editor.jsComponent)
  logit("Component(\"XXX\")", Editor.component("XXX").raw)

  trait RowData extends js.Object {
    val make: String
    val model: String
    val year: Int
  }

  object RowData {
    def apply(make: String, model: String, year: Int): RowData =
      js.Dynamic.literal("make" -> make, "model" -> model, "year" -> year).asInstanceOf[RowData]
  }

  class Backend(@unused $ : BackendScope[Unit, Unit]) {
    def doSomething(): Unit = dom.console.log("DO SOMETHING!")

    def render() = {
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
              .field("make")
              .editable(true)
              .cellEditor("agSelectCellEditor")
              .cellEditorParams(
                js.Dynamic.literal("values" -> js.Array("Fender", "Gibson", "Ibanez"))
              ),
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
  }
  val component =
    ScalaComponent
      .builder[Unit]
      .backend(new Backend(_))
      .renderBackend
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

    val comp = component()
    Editor.logit("Component:", comp)

    val mounted = comp.renderIntoDOM(container)

    logit("Mounted", mounted)
    logit("Mounted backend", mounted.backend)
    mounted.backend.doSomething()
    ()
  }
}

@JSExportTopLevel("Editor")
object Editor {
  def logit(message: String, obj: Any) = dom.console.log(message, obj.asInstanceOf[js.Any])

  // @JSExportAll
  class Backend() {
    def render(p: String): VdomNode =
      <.input(^.defaultValue := p, ^.width := "100%")

    // @JSExport
    def getValue(): js.Any = "Edited"

    def isCancelBeforeStart() = { println("isCancelBeforeStart() called"); false }
    def isCancelAfterEnd() = { println("isCancelAfterEnd called"); false }
    def afterGuiAttached() = println("Gui Attached")
  }

  def fromProps(q: ICellEditorParams): String = q.value.asInstanceOf[String] + "!"

  // val component = ScalaComponent.builder[String].renderBackend[Backend].build
  val component = ScalaComponent.builder[String].renderBackend[Backend].myBuild

  // @JSExport
  val jsComponent = component
    .cmapCtorProps[ICellEditorParams](fromProps)
    .toJsComponent
    .raw

  import japgolly.scalajs.react.component._
  import japgolly.scalajs.react.component.builder._
  import japgolly.scalajs.react.component.Scala._
  import japgolly.scalajs.react.internal._

  implicit class Step4Ops[P, C <: Children, S, B, US <: UpdateSnapshot](
    step4: Builder.Step4[P, C, S, B, US]
  ) {
    type SnapshotValue = US#Value
    def myBuild(implicit
      ctorType:   CtorType.Summoner[Box[P], C],
      snapshotJs: JsRepr[SnapshotValue]
    ): Scala.Component[P, S, B, ctorType.CT] = {
      val c = ViaReactComponent(step4)(snapshotJs)
      dom.console.log("ViaReactCompent")
      dom.console.log(c.asInstanceOf[js.Any])
      myFromReactComponentClass(c)(ctorType)
    }

    def myFromReactComponentClass(
      rc:                raw.React.ComponentClass[Box[P], Box[S]]
    )(implicit ctorType: CtorType.Summoner[Box[P], C]): Scala.Component[P, S, B, ctorType.CT] =
      Js.component[Box[P], C, Box[S]](rc)(ctorType)
        .addFacade[Scala.Vars[P, S, B]]
        .cmapCtorProps[P](Box(_))
        .mapUnmounted { u =>
          dom.console.log("mapUnmounted")
          dom.console.log(u.asInstanceOf[js.Any])
          u.mapUnmountedProps(_.unbox)
            .mapMounted { m =>
              dom.console.log("mapMounted")
              dom.console.log(m.asInstanceOf[js.Any])
              println("Here I AM!!!!!")
              val mr = Scala.mountedRoot(m)
              dom.console.log("mountedRoot")
              dom.console.log(mr.asInstanceOf[js.Any])
              mr
            }
        }
    // def myFromReactComponentClass(
    //   rc:                raw.React.ComponentClass[Box[P], Box[S]]
    // )(implicit ctorType: CtorType.Summoner[Box[P], C]): Scala.Component[P, S, B, ctorType.CT] =
    //   Js.component[Box[P], C, Box[S]](rc)(ctorType)
    //     .addFacade[Scala.Vars[P, S, B]]
    //     .cmapCtorProps[P](Box(_))
    //     .mapUnmounted(
    //       _.mapUnmountedProps(_.unbox)
    //         .mapMounted(Scala.mountedRoot)
    //     )

  }

  @js.native
  trait MyVars[PP, SS, BB] extends js.Object with Scala.Vars[PP, SS, BB] {
    var mountedImpure: MountedImpure[PP, SS, BB]
    var mountedPure: MountedPure[PP, SS, BB]
    var backend: BB
    val getValue: js.Function0[js.Any] = () => "Phred"
  }
}
