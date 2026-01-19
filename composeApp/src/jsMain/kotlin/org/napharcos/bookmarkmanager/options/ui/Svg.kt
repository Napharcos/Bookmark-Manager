package org.napharcos.bookmarkmanager.options.ui

fun addIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M440-440H200v-80h240v-240h80v240h240v80H520v240h-80v-240Z\"/></svg>"
}

fun trashIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360ZM280-720v520-520Z\"/></svg>"
}

fun importIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M200-80q-33 0-56.5-23.5T120-160v-640q0-33 23.5-56.5T200-880h360l280 280v440q0 33-23.5 56.5T760-80H200Zm320-560v-160H200v640h560v-480H520ZM480-520l120 120-56 56-64-64v168h-80v-168l-64 64-56-56 120-120ZM200-800v160-160Z\"/></svg>"
}

fun exportIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M200-80q-33 0-56.5-23.5T120-160v-640q0-33 23.5-56.5T200-880h360l280 280v440q0 33-23.5 56.5T760-80H200Zm320-560v-160H200v640h560v-480H520ZM480-240l-120-120 56-56 64 64v-168h80v168l64-64 56 56-120 120ZM200-800v160-160Z\"/></svg>"
}

fun backgroundIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M200-120q-33 0-56.5-23.5T120-200v-240h80v240h240v80H200Zm320 0v-80h240v-240h80v240q0 33-23.5 56.5T760-120H520ZM240-280l120-160 90 120 120-160 150 200H240ZM120-520v-240q0-33 23.5-56.5T200-840h240v80H200v240h-80Zm640 0v-240H520v-80h240q33 0 56.5 23.5T840-760v240h-80Zm-140-40q-26 0-43-17t-17-43q0-26 17-43t43-17q26 0 43 17t17 43q0 26-17 43t-43 17Z\"/></svg>"
}

fun contrastIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm40-83q119-15 199.5-104.5T800-480q0-123-80.5-212.5T520-797v634Z\"/></svg>"
}

fun contrastOffIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M819-28 701-146q-48 32-103.5 49T480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-62 17-117.5T146-701L27-820l57-57L876-85l-57 57ZM480-160q45 0 85.5-12t76.5-33L480-367v207Zm335-100-59-59q21-35 32.5-75.5T800-480q0-133-93.5-226.5T480-800v205L260-815q48-31 103.5-48T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 61-17 116.5T815-260Z\"/></svg>"
}

fun librariesIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M240-80q-50 0-85-35t-35-85v-120h120v-560h600v680q0 50-35 85t-85 35H240Zm480-80q17 0 28.5-11.5T760-200v-600H320v480h360v120q0 17 11.5 28.5T720-160ZM360-600v-80h360v80H360Zm0 120v-80h360v80H360ZM240-160h360v-80H200v40q0 17 11.5 28.5T240-160Zm0 0h-40 400-360Z\"/></svg>"
}

fun termsIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"M480-80q-139-35-229.5-159.5T160-516v-244l320-120 320 120v244q0 85-29 163.5T688-214L560-342q-18 11-38.5 16.5T480-320q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 22-5.5 42.5T618-398l60 60q20-41 31-86t11-92v-189l-240-90-240 90v189q0 121 68 220t172 132q26-8 49.5-20.5T576-214l56 56q-33 27-71.5 47T480-80Zm0-320q33 0 56.5-23.5T560-480q0-33-23.5-56.5T480-560q-33 0-56.5 23.5T400-480q0 33 23.5 56.5T480-400Zm8-77Z\"/></svg>"
}

fun folderManagerIcon(hexColor: String): String {
    return "<svg xmlns=\"http://www.w3.org/2000/svg\" height=\"22px\" viewBox=\"0 -960 960 960\" width=\"22px\" fill=\"#$hexColor\"><path d=\"m680-80-12-60q-12-5-22.5-10.5T624-164l-58 18-40-68 46-40q-2-12-2-26t2-26l-46-40 40-68 58 18q11-8 21.5-13.5T668-420l12-60h80l12 60q12 5 22.5 10.5T816-396l58-18 40 68-46 40q2 12 2 26t-2 26l46 40-40 68-58-18q-11 8-21.5 13.5T772-140l-12 60h-80Zm40-120q33 0 56.5-23.5T800-280q0-33-23.5-56.5T720-360q-33 0-56.5 23.5T640-280q0 33 23.5 56.5T720-200Zm-560-40v-480 172-12 320Zm0 80q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h240l80 80h320q33 0 56.5 23.5T880-640v131q-18-13-38-22.5T800-548v-92H447l-80-80H160v480h283q3 21 9.5 41t15.5 39H160Z\"/></svg>"
}