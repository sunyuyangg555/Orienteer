/**
 * Static class which contains utility methods.
 */
var OArchitectUtil = {
    createOClassVertex: function (oClass, x, y) {
        if (x === undefined) x = 0;
        if (y === undefined) y = 0;
        var vertex = new mxCell(oClass,
            new mxGeometry(x, y, OArchitectConstants.OCLASS_WIDTH, OArchitectConstants.OCLASS_HEIGHT));
        vertex.setVertex(true);
        vertex.setStyle(OArchitectConstants.OCLASS_EDITOR_STYLE);
        return vertex;
    },

    createOPropertyVertex: function (property) {
        var vertex = new mxCell(property,
            new mxGeometry(0, 0, 0, OArchitectConstants.OPROPERTY_HEIGHT), OArchitectConstants.OPROPERTY_EDITOR_STYLE);
        vertex.setVertex(true);
        return vertex;
    },

    getOClassesAsJSON: function (graph) {
        var withParents = [];
        var withoutParents = [];
        var cells = graph.getChildVertices(graph.getDefaultParent());

        OArchitectUtil.forEach(cells, function (cell) {
            var oClass = cell.value;
            if (oClass.isSubClass()) withParents.push(oClass.toJson());
            else withoutParents.push(oClass.toJson());
        });

        return '[' + withoutParents.concat(withParents).join(',\n') + ']';
    },

    getEditorXmlNode: function (graph) {
        var encoder = new mxCodec();
        return encoder.encode(graph.getModel());
    },

    getAllCells: function () {
        var graph = app.editor.graph;
        return graph.getChildVertices(graph.getDefaultParent());
    },

    getSuperClassesCells: function (graph, oClass) {
        var superClasses = oClass.superClasses;
        var superClassesCells = [];
        if (superClasses != null) {
            OArchitectUtil.forEach(superClasses, function (superClass) {
                superClassesCells.push(superClass.cell);
            });
        }
        return superClassesCells;
    },

    getSubClassesCells: function (graph, oClass) {
        var subClasses = oClass.subClasses;
        var subClassesCells = [];
        if (subClasses != null) {
            OArchitectUtil.forEach(subClasses, function (subClass) {
                subClassesCells.push(subClass.cell);
            });
        }
        return subClassesCells;
    },

    getCellsByClassNames: function (classNames) {
        var result = [];
        var cells = OArchitectUtil.getAllCells();
        OArchitectUtil.forEach(cells, function (cell) {
            var oClass = cell.value;
            if (oClass != null && classNames.indexOf(oClass.name) > -1) {
                result.push(cell);
            }
        });

        return result;
    },

    getCellByClassName: function (className) {
        var result = null;
        var cells = OArchitectUtil.getAllCells();
        OArchitectUtil.forEach(cells, function (cell, trigger) {
            if (cell.value.name === className) {
                result = cell;
                trigger.stop = true;
            }
        });
        return result;
    },

    getClassPropertiesCells: function (oClass) {
        var graph = app.editor.graph;
        var result = graph.getChildVertices(oClass.cell);
        return result != null ? result : [];
    },

    fromJsonToOClasses: function (json) {
        var classes = [];
        var jsonClasses = JSON.parse(json);
        if (jsonClasses !== null && jsonClasses.length > 0) {
            for (var i = 0; i < jsonClasses.length; i++) {
                var oClass = new OArchitectOClass();
                oClass.config(jsonClasses[i]);
                classes.push(oClass);
            }
        }
        return classes;
    },

    getClassByPropertyCell: function (graph, cell) {
        if (cell.value instanceof OArchitectOClass)
            return cell;
        if (cell === graph.getDefaultParent())
            return null;
        return this.getClassByPropertyCell(graph, graph.getModel().getParent(cell));
    },

    existsOClassInGraph: function (graph, className) {
        var exists = false;
        var cells = graph.getChildVertices(graph.getDefaultParent());
        OArchitectUtil.forEach(cells, function (cell, trigger) {
            if (cell.value.name === className) {
                exists = true;
                trigger.stop = true;
            }
        });
        return exists;
    },

    forEach: function (arr, func) {
        if (arr != null && arr.length > 0 && func != null) {
            var trigger = {
                stop: false
            };
            for (var i = 0; i < arr.length; i++) {
                func(arr[i], trigger);
                if (trigger.stop) break;
            }
        }
    },

    /**
     * Creates function for save {@link OArchitectOClass} and {@link OArchitectOProperty} to editor xml configFromJson.
     * Overrides {@link mxObjectCodec#writeComplexAttribute}
     */
    createWriteComplexAttributeFunction: function () {
        var defaultBehavior = mxObjectCodec.prototype.writeComplexAttribute;
        return function (enc, obj, name, value, node) {
            if (value instanceof OArchitectOClass || value instanceof OArchitectOProperty) {
                value = value.toEditorConfigObject();
            } else if (name === 'cell' || name === 'configuredFromEditorConfig') {
                value = undefined;
            }

            defaultBehavior.apply(this, arguments);
        };
    },
    
    createDecodeFunction: function () {
        var defaultBehavior = mxCodec.prototype.decode;
        
        return function (node, into) {
            var result = defaultBehavior.apply(this, arguments);
            if (into instanceof mxGraphModel) {
                var graph = app.editor.graph;
                var classCells = graph.getChildVertices(graph.getDefaultParent());
                OArchitectUtil.forEach(classCells, function (classCell) {
                    var oClass = classCell.value;
                    oClass.configFromEditorConfig(classCell);
                });
            }
            return result;
        }
    }
};
