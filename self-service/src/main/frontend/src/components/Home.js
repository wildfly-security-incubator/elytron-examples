/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Button} from 'reactstrap'

import {nextPath} from "../history"

import './Home.css'

const TableRow = ({name, attributes, deleteRow}) => {
    const updateColumn =
        <td>
            <span className="glyphicon glyphicon-pencil hover" onClick={() => {
                nextPath('/attribute/update/' + encodeURIComponent(name))
            }}/>
        </td>
    const valuesList =
        <ul>
            {
                Object.keys(attributes[name]).map((x, y) => {
                    return (
                        <li key={attributes[name][x].id}>
                            {attributes[name][x].name}
                        </li>
                    )
                })
            }
        </ul>
    const attributeValues = attributes[name].every(current => current.name === "") ? <strong>EMPTY</strong> : valuesList
    return (
        <tr key={name}>
            <td>{name}</td>
            <td>{attributeValues}</td>
            {name !== "Roles" ? updateColumn : <td></td> }
        </tr>
    )
}

class Home extends Component {

    render() {
        if (!this.props.attributes) {
            return null
        }
        return (
            <>
                <h2>{this.props.name}</h2>
                <Button type="button" className="btn btn-primary mb-2 mr-2 to-the-right" onClick={() => {
                    nextPath('/password/update')
                }}>Change Password</Button>
                <table className="table table-hover table-striped">
                    <thead>
                        <tr>
                            <th>Attribute</th>
                            <th>Values</th>
                            <th><span/></th>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            Object.keys(this.props.attributes)
                                .sort((a,b) => {
                                    if (a === "Roles") {
                                        return -1
                                    } else if (b === "Roles"){
                                        return 1
                                    } else {
                                        return a.localeCompare(b)
                                    }
                                })
                                .map(name => {
                                    return <TableRow key={name} name={name} attributes={this.props.attributes}/>
                                })
                        }
                    </tbody>
                </table>
            </>
        )
    }
}

export default connect(
    (state) => ({
        name: state.identityData.name,
        attributes: state.identityData.attributes
    }),
)(Home)
